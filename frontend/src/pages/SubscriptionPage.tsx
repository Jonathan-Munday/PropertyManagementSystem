import { useCallback, useEffect, useState } from "react";
import { Check, CreditCard } from "lucide-react";
import { PageIntro } from "../components/layout/PageIntro.tsx";
import { Button } from "../components/ui/Button.tsx";
import { Modal } from "../components/ui/Modal.tsx";
import { StatCard } from "../components/ui/StatCard.tsx";
import { useAuth } from "../context/AuthContext.tsx";
import { formatDate, formatZar } from "../lib/format.ts";
import { canManageBilling, canViewBilling, roleLabel } from "../lib/permissions.ts";
import {
  cancelSubscription,
  changeSubscription,
  fetchPlans,
  fetchSubscription,
  type OrganisationSubscription,
  type SubscriptionPlan,
} from "../services/billing.ts";
import { toErrorMessage } from "../services/api.ts";

const included = [
  "Properties, tenants, and leases",
  "Rent recording on the payment ledger",
  "Maintenance requests",
  "Organisation team and roles",
];

function usageLabel(current: number, maximum: number | null, noun: string) {
  if (maximum == null) return `${current} ${noun}. Additions stay blocked until a package is selected.`;
  return `${current} of ${maximum} ${noun}`;
}

export function SubscriptionPage() {
  const auth = useAuth();
  const role = auth.selectedOrganisation?.role;
  const canView = canViewBilling(role);
  const canChange = canManageBilling(role);
  const [plans, setPlans] = useState<SubscriptionPlan[]>([]);
  const [subscription, setSubscription] = useState<OrganisationSubscription | null>(null);
  const [loading, setLoading] = useState(true);
  const [busyCode, setBusyCode] = useState<string | null>(null);
  const [checkoutCode, setCheckoutCode] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!canView) {
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const [nextPlans, nextSubscription] = await Promise.all([fetchPlans(), fetchSubscription()]);
      setPlans(nextPlans);
      setSubscription(nextSubscription);
    } catch (loadError) {
      setError(toErrorMessage(loadError));
    } finally {
      setLoading(false);
    }
  }, [canView]);

  useEffect(() => {
    void load();
  }, [load]);

  async function finishCheckout(outcome: "SUCCESS" | "CANCELED" | "FAILED") {
    if (!checkoutCode) return;
    setBusyCode(checkoutCode);
    setError(null);
    setNotice(null);
    try {
      const next = await changeSubscription(checkoutCode, outcome);
      setSubscription(next);
      setNotice("Simulated checkout complete. The selected package is a demo entitlement and no money was charged.");
      setCheckoutCode(null);
    } catch (changeError) {
      setError(toErrorMessage(changeError));
      setCheckoutCode(null);
    } finally {
      setBusyCode(null);
    }
  }

  async function cancel() {
    setBusyCode("cancel");
    setError(null);
    try {
      setSubscription(await cancelSubscription());
    } catch (cancelError) {
      setError(toErrorMessage(cancelError));
    } finally {
      setBusyCode(null);
    }
  }

  if (!canView) {
    return (
      <div className="mx-auto max-w-3xl">
        <PageIntro description="Software subscription for this organisation. Rent payments stay on the Payments page." />
        <p className="panel px-4 py-3 text-sm leading-6 text-ink-soft">
          Your {roleLabel(role) || "current"} role cannot view subscription billing. An owner or admin can.
        </p>
      </div>
    );
  }

  const currentCode = subscription?.plan?.code ?? null;
  const mode = subscription?.billingMode ?? "NONE";
  const demo = subscription?.demoCheckout !== false;

  return (
    <div className="mx-auto max-w-6xl">
      <PageIntro description="Software subscription for this organisation. Rent payments stay on the Payments page.">
        {canChange && mode !== "NONE" && mode !== "CANCELED" ? (
          <Button variant="secondary" onClick={() => void cancel()} disabled={busyCode !== null}>
            {busyCode === "cancel" ? "Canceling…" : "Cancel package"}
          </Button>
        ) : null}
      </PageIntro>

      <div className="mb-6 rounded-xl border border-amber/30 bg-amber-soft px-4 py-3 text-sm leading-6 text-ink">
        {demo
          ? "Demo checkout. Portico does not collect card details here, and a package change is not a paid subscription."
          : "Live billing is enabled for this server."}
        {subscription?.credentialsPresent && !subscription.liveChargesEnabled
          ? " Payment credentials are present on the server, but there is no verified payment webhook, so checkout stays in demo mode."
          : ""}
      </div>

      {!canChange ? (
        <p className="mb-6 text-sm leading-6 text-muted">
          Your {roleLabel(role)} role can view billing. Only an owner can purchase, change, or cancel the subscription.
        </p>
      ) : null}

      {notice ? (
        <p className="mb-6 rounded-xl border border-teal/30 bg-teal-soft px-4 py-3 text-sm leading-6 text-ink" role="status">
          {notice}
        </p>
      ) : null}

      {error ? (
        <p className="mb-6 rounded-xl border border-clay/30 bg-clay-soft px-4 py-3 text-sm leading-6 text-clay" role="alert">
          {error}
        </p>
      ) : null}

      {loading ? <p className="text-sm text-muted">Loading the subscription…</p> : null}

      {subscription ? (
        <>
          <div className="mb-6 grid gap-4 sm:grid-cols-2">
            <StatCard
              label="Current package"
              value={subscription.plan?.name ?? "None"}
              detail={subscription.detail}
              icon={<CreditCard size={18} />}
              iconClass="bg-teal-soft text-teal-deep"
            />
            <StatCard
              label="Usage"
              value={mode === "NONE" ? "No cap" : mode}
              detail={`${usageLabel(subscription.usage.properties, subscription.usage.maxProperties, "properties")}. ${usageLabel(subscription.usage.users, subscription.usage.maxUsers, "members")}.`}
              icon={<Check size={18} />}
              iconClass="bg-navy-soft text-navy"
            />
          </div>

          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            {plans.map((item) => {
              const current = item.code === currentCode && mode !== "CANCELED" && mode !== "NONE";
              const over = subscription.usage.properties > item.maxProperties || subscription.usage.users > item.maxUsers;
              return (
                <article key={item.code} className={`panel flex flex-col p-5 ${current ? "ring-2 ring-teal" : ""}`}>
                  <p className="text-sm text-muted">{item.name}</p>
                  <p className="num mt-2 font-display text-3xl text-navy">{formatZar(item.amount)}</p>
                  <p className="text-xs text-muted">per month</p>
                  <ul className="mt-4 space-y-2 text-sm leading-5 text-ink-soft">
                    <li>{item.maxProperties} properties</li>
                    <li>{item.maxUsers} team members</li>
                  </ul>
                  <Button
                    className="mt-5"
                    variant={current ? "secondary" : "primary"}
                    disabled={!canChange || current || busyCode !== null || (over && !current)}
                    onClick={() => {
                      setError(null);
                      setNotice(null);
                      setCheckoutCode(item.code);
                    }}
                  >
                    {busyCode === item.code
                      ? "Saving…"
                      : current
                        ? "Current package"
                        : over
                          ? "Usage is above this cap"
                          : mode === "NONE" || mode === "CANCELED"
                            ? "Choose package"
                            : item.amount > (subscription.plan?.amount ?? 0)
                              ? "Upgrade"
                              : "Downgrade"}
                  </Button>
                </article>
              );
            })}
          </div>

          <div className="panel mt-6 overflow-x-auto">
            <table className="w-full min-w-[720px] text-left text-sm">
              <thead className="text-muted">
                <tr>
                  <th className="px-4 py-3 font-medium">Included</th>
                  {plans.map((item) => (
                    <th key={item.code} className="px-4 py-3 font-medium">
                      {item.name}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                <tr>
                  <th className="px-4 py-3 font-medium">Monthly price</th>
                  {plans.map((item) => (
                    <td key={item.code} className="num px-4 py-3">
                      {formatZar(item.amount)}
                    </td>
                  ))}
                </tr>
                <tr>
                  <th className="px-4 py-3 font-medium">Properties</th>
                  {plans.map((item) => (
                    <td key={item.code} className="num px-4 py-3">
                      {item.maxProperties}
                    </td>
                  ))}
                </tr>
                <tr>
                  <th className="px-4 py-3 font-medium">Team members</th>
                  {plans.map((item) => (
                    <td key={item.code} className="num px-4 py-3">
                      {item.maxUsers}
                    </td>
                  ))}
                </tr>
                {included.map((feature) => (
                  <tr key={feature}>
                    <th className="px-4 py-3 font-medium">{feature}</th>
                    {plans.map((item) => (
                      <td key={item.code} className="px-4 py-3 text-teal-deep">
                        Included
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <p className="mt-4 text-xs leading-5 text-muted">
            These monthly rand prices are proposed package amounts, benchmarked against published South African property-management products. They are not a guarantee that Portico costs less than every competitor.
          </p>

          <section className="mt-8">
            <h2 className="font-display text-2xl text-navy">Status</h2>
            {subscription.history.length === 0 ? (
              <p className="mt-3 text-sm text-muted">No subscription changes have been recorded.</p>
            ) : (
              <ul className="mt-3 divide-y divide-line overflow-hidden rounded-xl border border-line bg-card">
                {subscription.history.map((item, index) => (
                  <li key={`${item.issuedOn}-${item.planCode}-${index}`} className="px-4 py-3 text-sm">
                    <p className="font-medium">{item.planCode ?? "Package"} · {item.status}</p>
                    <p className="mt-1 text-muted">
                      {formatDate(item.issuedOn)} · {formatZar(item.amount)} · {item.provider || "none"} · {item.detail}
                    </p>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </>
      ) : null}

      <Modal
        open={checkoutCode !== null}
        title="Simulated checkout"
        description="Portico does not collect card details. This checkout does not charge money."
        onClose={() => {
          if (busyCode === null) setCheckoutCode(null);
        }}
      >
        <p className="text-sm leading-6 text-ink-soft">
          {plans.find((item) => item.code === checkoutCode)?.name ?? "This package"} stays a demo entitlement until a real payment provider is connected. Choose the simulated result.
        </p>
        <div className="mt-4 flex flex-col gap-2">
          <Button onClick={() => void finishCheckout("SUCCESS")} disabled={busyCode !== null}>
            {busyCode ? "Saving…" : "Simulate successful checkout"}
          </Button>
          <Button variant="secondary" onClick={() => void finishCheckout("CANCELED")} disabled={busyCode !== null}>
            Simulate canceled checkout
          </Button>
          <Button variant="danger" onClick={() => void finishCheckout("FAILED")} disabled={busyCode !== null}>
            Simulate failed checkout
          </Button>
        </div>
      </Modal>
    </div>
  );
}
