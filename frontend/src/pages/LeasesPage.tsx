import { useEffect, useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Pencil, Plus, Trash2 } from "lucide-react";
import { z } from "zod";
import { usePortfolio } from "../context/PortfolioContext.tsx";
import { formatDate, formatZar, fullName, leaseTermStatus, parseMoney, plusYears, todayIso } from "../lib/format.ts";
import { buildLeaseRecord } from "../lib/leaseRecord.ts";
import { createId } from "../lib/ids.ts";
import type { Lease } from "../types/index.ts";
import { PageIntro } from "../components/layout/PageIntro.tsx";
import { Button } from "../components/ui/Button.tsx";
import { ConfirmDialog } from "../components/ui/ConfirmDialog.tsx";
import { EmptyState } from "../components/ui/EmptyState.tsx";
import { controlClass, Field } from "../components/ui/Field.tsx";
import { FilterChip } from "../components/ui/FilterChip.tsx";
import { Modal } from "../components/ui/Modal.tsx";
import { StatusBadge } from "../components/ui/StatusBadge.tsx";

const leaseSchema = z
  .object({
    propertyId: z.string().min(1, "Choose a property"),
    tenantId: z.string().min(1, "Choose a tenant"),
    monthlyRent: z
      .string()
      .trim()
      .min(1, "Enter the monthly rent")
      .refine((value) => parseMoney(value) > 0, "Monthly rent must be greater than zero"),
    startDate: z.string().min(1, "Choose a start date"),
    endDate: z.string().min(1, "Choose an end date"),
  })
  .refine((value) => value.endDate >= value.startDate, {
    message: "The end date must be on or after the start date",
    path: ["endDate"],
  });

type LeaseFormValues = z.infer<typeof leaseSchema>;
type LeaseFilter = "active" | "ended" | "review" | "all";

function openTerm(lease: Lease) {
  const status = leaseTermStatus(lease);
  return status === "Active" || status === "Upcoming";
}

export function LeasesPage() {
  const { leases, properties, tenants, saveLease, deleteLease } = usePortfolio();
  const [filter, setFilter] = useState<LeaseFilter>("active");
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Lease | null>(null);
  const [removing, setRemoving] = useState<Lease | null>(null);
  const [busy, setBusy] = useState(false);

  const filtered = useMemo(() => {
    return [...leases]
      .filter((lease) => {
        const status = leaseTermStatus(lease);
        if (filter === "active") return status === "Active" || status === "Upcoming";
        if (filter === "ended") return status === "Ended";
        if (filter === "review") return status === "Needs dates";
        return true;
      })
      .sort((a, b) => (b.startDate || "").localeCompare(a.startDate || ""));
  }, [leases, filter]);

  const activeCount = leases.filter((lease) => openTerm(lease)).length;
  const endedCount = leases.filter((lease) => leaseTermStatus(lease) === "Ended").length;
  const reviewCount = leases.filter((lease) => leaseTermStatus(lease) === "Needs dates").length;

  async function confirmRemove() {
    if (!removing) return;
    setBusy(true);
    const ok = await deleteLease(removing.leaseId);
    setBusy(false);
    if (ok) setRemoving(null);
  }

  return (
    <div>
      <PageIntro description="Agreements that connect a tenant to a property, with the monthly rent and the term.">
        <Button
          onClick={() => {
            setEditing(null);
            setOpen(true);
          }}
          disabled={properties.length === 0 || tenants.length === 0}
        >
          <Plus size={16} /> New lease
        </Button>
      </PageIntro>

      {properties.length === 0 || tenants.length === 0 ? (
        <p className="mb-4 rounded-xl border border-line bg-card px-4 py-3 text-sm text-muted">
          Add at least one property and one tenant before creating a lease.
        </p>
      ) : null}

      <div className="mb-4 flex flex-wrap gap-2">
        <FilterChip pressed={filter === "active"} onClick={() => setFilter("active")}>
          Active ({activeCount})
        </FilterChip>
        <FilterChip pressed={filter === "ended"} onClick={() => setFilter("ended")}>
          Ended ({endedCount})
        </FilterChip>
        <FilterChip pressed={filter === "review"} onClick={() => setFilter("review")}>
          Needs dates ({reviewCount})
        </FilterChip>
        <FilterChip pressed={filter === "all"} onClick={() => setFilter("all")}>
          All ({leases.length})
        </FilterChip>
      </div>

      {leases.length === 0 ? (
        <EmptyState title="No leases yet" body="Create an agreement once a property and a tenant are on file." />
      ) : filtered.length === 0 ? (
        <EmptyState title="Nothing in this view" body="Switch the filter to see active or ended agreements." />
      ) : (
        <div className="panel overflow-hidden">
          <div className="overflow-x-auto">
            <table className="data-table w-full min-w-[840px] text-left text-sm">
              <thead className="text-muted">
                <tr>
                  <th className="px-4 py-3 font-medium">Lease</th>
                  <th className="px-4 py-3 font-medium">Property</th>
                  <th className="px-4 py-3 font-medium">Tenant</th>
                  <th className="px-4 py-3 font-medium">Term</th>
                  <th className="px-4 py-3 text-right font-medium">Monthly rent</th>
                  <th className="px-4 py-3 font-medium">Status</th>
                  <th className="px-4 py-3 text-right font-medium">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {filtered.map((lease) => {
                  const property = properties.find((item) => item.propertyId === lease.propertyId);
                  const status = leaseTermStatus(lease);
                  return (
                    <tr key={lease.leaseId} className="hover:bg-paper/70">
                      <td className="px-4 py-3 font-medium">{lease.leaseId}</td>
                      <td className="px-4 py-3">
                        <p>{property?.address ?? "Unknown property"}</p>
                        <p className="text-xs text-muted">{lease.propertyId}</p>
                      </td>
                      <td className="px-4 py-3">{lease.tenantName}</td>
                      <td className="px-4 py-3 text-muted">
                        {lease.startDate && lease.endDate
                          ? `${formatDate(lease.startDate)} – ${formatDate(lease.endDate)}`
                          : "Dates required"}
                      </td>
                      <td className="num px-4 py-3 text-right font-medium">{formatZar(lease.monthlyRent)}</td>
                      <td className="px-4 py-3">
                        <StatusBadge status={status} />
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex justify-end gap-1">
                          <Button
                            variant="ghost"
                            size="icon"
                            aria-label={`Edit lease ${lease.leaseId}`}
                            onClick={() => {
                              setEditing(lease);
                              setOpen(true);
                            }}
                          >
                            <Pencil size={16} />
                          </Button>
                          <Button
                            variant="ghost"
                            size="icon"
                            aria-label={`Remove lease ${lease.leaseId}`}
                            onClick={() => setRemoving(lease)}
                          >
                            <Trash2 size={16} />
                          </Button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <Modal
        open={open}
        title={editing ? "Edit lease" : "New lease"}
        description="Link a tenant to a property and set the rent and term."
        onClose={() => setOpen(false)}
        widthClass="max-w-2xl"
      >
        {open ? (
          <LeaseForm key={editing?.leaseId ?? "new"} lease={editing} onClose={() => setOpen(false)} onSave={saveLease} />
        ) : null}
      </Modal>

      <ConfirmDialog
        open={removing !== null}
        title="Remove lease"
        body={removing ? `Remove lease ${removing.leaseId} for ${removing.tenantName}? Payments already recorded keep this lease id.` : ""}
        confirmLabel="Remove lease"
        busy={busy}
        onConfirm={() => void confirmRemove()}
        onClose={() => setRemoving(null)}
      />
    </div>
  );
}

function LeaseForm({
  lease,
  onClose,
  onSave,
}: {
  lease: Lease | null;
  onClose: () => void;
  onSave: (lease: Lease, mode: "create" | "update") => Promise<boolean>;
}) {
  const { properties, tenants } = usePortfolio();
  const start = todayIso();
  const knownTenant = tenants.find((tenant) => tenant.tenantId === lease?.tenantId);
  const matchedByName = !lease?.tenantId
    ? tenants.find((tenant) => fullName(tenant) === lease?.tenantName)
    : undefined;

  const form = useForm<LeaseFormValues>({
    resolver: zodResolver(leaseSchema),
    defaultValues: {
      propertyId: lease?.propertyId ?? "",
      tenantId: lease?.tenantId || matchedByName?.tenantId || "",
      monthlyRent: lease ? String(lease.monthlyRent) : "",
      startDate: lease ? (lease.startDate ?? "") : start,
      endDate: lease ? (lease.endDate ?? "") : plusYears(start, 1),
    },
  });

  const propertyId = form.watch("propertyId");
  const { setValue } = form;

  useEffect(() => {
    if (lease) return;
    const selected = properties.find((property) => property.propertyId === propertyId);
    if (selected) {
      setValue("monthlyRent", String(selected.rentalAmount), { shouldValidate: true });
    }
  }, [propertyId, lease, properties, setValue]);

  const tenantChoices = useMemo(() => {
    const choices = tenants.map((tenant) => ({ value: tenant.tenantId, label: fullName(tenant) }));
    if (lease?.tenantId && !knownTenant) {
      choices.unshift({
        value: lease.tenantId,
        label: lease.tenantName ? `${lease.tenantName} (not on file)` : lease.tenantId,
      });
    }
    return choices;
  }, [tenants, lease, knownTenant]);

  const selectedProperty = properties.find((property) => property.propertyId === lease?.propertyId);

  const onSubmit = form.handleSubmit(async (values) => {
    const tenant = tenants.find((item) => item.tenantId === values.tenantId);
    const tenantName = tenant ? fullName(tenant) : lease?.tenantName ?? "";
    if (!tenant && !lease?.tenantId) {
      form.setError("tenantId", { message: "Choose a tenant" });
      return;
    }
    const entity = buildLeaseRecord({
      existing: lease,
      leaseId: lease?.leaseId ?? createId("LSE"),
      propertyId: values.propertyId,
      tenantId: values.tenantId,
      tenantName,
      monthlyRent: parseMoney(values.monthlyRent),
      startDate: values.startDate,
      endDate: values.endDate,
    });
    if (!entity?.tenantId) {
      form.setError("tenantId", { message: "Choose a tenant" });
      return;
    }
    const ok = await onSave(entity, lease ? "update" : "create");
    if (ok) onClose();
  });

  return (
    <form noValidate onSubmit={onSubmit} className="space-y-4">
      {lease ? (
        <Field label="Property" htmlFor="lease-property">
          <input type="hidden" id="lease-property" {...form.register("propertyId")} />
          <p className="rounded-xl border border-line bg-paper px-3 py-2 text-sm">
            {selectedProperty?.address ?? lease.propertyId}
          </p>
          <p className="mt-1 text-xs text-muted">A lease stays on the property it was opened against.</p>
        </Field>
      ) : (
        <Field label="Property" htmlFor="lease-property" error={form.formState.errors.propertyId?.message}>
          <select id="lease-property" className={controlClass} {...form.register("propertyId")}>
            <option value="">Select a property</option>
            {[...properties]
              .sort((a, b) => a.address.localeCompare(b.address))
              .map((property) => (
                <option key={property.propertyId} value={property.propertyId}>
                  {property.address} · {property.available ? "available" : "occupied"}
                </option>
              ))}
          </select>
        </Field>
      )}
      <Field label="Tenant" htmlFor="lease-tenant" error={form.formState.errors.tenantId?.message}>
        <select id="lease-tenant" className={controlClass} {...form.register("tenantId")}>
          <option value="">Select a tenant</option>
          {tenantChoices.map((choice) => (
            <option key={choice.value} value={choice.value}>
              {choice.label}
            </option>
          ))}
        </select>
      </Field>
      <Field label="Monthly rent" htmlFor="lease-rent" error={form.formState.errors.monthlyRent?.message}>
        <input id="lease-rent" inputMode="decimal" className={controlClass} {...form.register("monthlyRent")} />
      </Field>
      <div className="grid gap-4 sm:grid-cols-2">
        <Field label="Start date" htmlFor="lease-start" error={form.formState.errors.startDate?.message}>
          <input id="lease-start" type="date" className={controlClass} {...form.register("startDate")} />
        </Field>
        <Field label="End date" htmlFor="lease-end" error={form.formState.errors.endDate?.message}>
          <input id="lease-end" type="date" className={controlClass} {...form.register("endDate")} />
        </Field>
      </div>
      <div className="flex flex-wrap justify-end gap-2 pt-2">
        <Button variant="secondary" onClick={onClose}>
          Cancel
        </Button>
        <Button type="submit" disabled={form.formState.isSubmitting || properties.length === 0 || tenants.length === 0}>
          {form.formState.isSubmitting ? "Saving…" : lease ? "Save changes" : "Create lease"}
        </Button>
      </div>
    </form>
  );
}
