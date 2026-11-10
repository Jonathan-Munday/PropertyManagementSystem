import { useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Pencil, Plus, Trash2 } from "lucide-react";
import { z } from "zod";
import { usePortfolio } from "../context/PortfolioContext.tsx";
import { formatDate, formatZar, parseMoney, todayIso } from "../lib/format.ts";
import { createId } from "../lib/ids.ts";
import type { Payment, PaymentStatus } from "../types/index.ts";
import { PageIntro } from "../components/layout/PageIntro.tsx";
import { Button } from "../components/ui/Button.tsx";
import { ConfirmDialog } from "../components/ui/ConfirmDialog.tsx";
import { EmptyState } from "../components/ui/EmptyState.tsx";
import { controlClass, Field } from "../components/ui/Field.tsx";
import { FilterChip } from "../components/ui/FilterChip.tsx";
import { Modal } from "../components/ui/Modal.tsx";
import { StatusBadge } from "../components/ui/StatusBadge.tsx";

const METHODS = ["EFT", "Debit order", "Card", "Cash"] as const;
const STATUSES = ["PAID", "PENDING", "OVERDUE"] as const;

const paymentSchema = z.object({
  leaseId: z.string().min(1, "Choose a lease"),
  amount: z
    .string()
    .trim()
    .min(1, "Enter the amount")
    .refine((value) => parseMoney(value) > 0, "Amount must be greater than zero"),
  paymentDate: z.string().min(1, "Choose a payment date"),
  paymentMethod: z.string().min(1, "Choose a method"),
  status: z.enum(STATUSES),
});

type PaymentFormValues = z.infer<typeof paymentSchema>;
type StatusFilter = "ALL" | PaymentStatus;

export function PaymentsPage() {
  const { payments, leases, properties, savePayment, deletePayment } = usePortfolio();
  const [filter, setFilter] = useState<StatusFilter>("ALL");
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Payment | null>(null);
  const [removing, setRemoving] = useState<Payment | null>(null);
  const [busy, setBusy] = useState(false);

  const filtered = useMemo(() => {
    return [...payments]
      .filter((payment) => filter === "ALL" || payment.status === filter)
      .sort((a, b) => b.paymentDate.localeCompare(a.paymentDate));
  }, [payments, filter]);

  function count(status: PaymentStatus) {
    return payments.filter((payment) => payment.status === status).length;
  }

  async function confirmRemove() {
    if (!removing) return;
    setBusy(true);
    const ok = await deletePayment(removing.paymentId);
    setBusy(false);
    if (ok) setRemoving(null);
  }

  return (
    <div>
      <PageIntro description="Payments against a lease, with status so paid, pending, and overdue amounts stay visible.">
        <Button
          onClick={() => {
            setEditing(null);
            setOpen(true);
          }}
          disabled={leases.length === 0}
        >
          <Plus size={16} /> Record payment
        </Button>
      </PageIntro>

      <div className="mb-4 flex flex-wrap gap-2">
        <FilterChip pressed={filter === "ALL"} onClick={() => setFilter("ALL")}>
          All ({payments.length})
        </FilterChip>
        <FilterChip pressed={filter === "PAID"} onClick={() => setFilter("PAID")}>
          Paid ({count("PAID")})
        </FilterChip>
        <FilterChip pressed={filter === "PENDING"} onClick={() => setFilter("PENDING")}>
          Pending ({count("PENDING")})
        </FilterChip>
        <FilterChip pressed={filter === "OVERDUE"} onClick={() => setFilter("OVERDUE")}>
          Overdue ({count("OVERDUE")})
        </FilterChip>
      </div>

      {leases.length === 0 ? (
        <p className="mb-4 rounded-xl border border-line bg-card px-4 py-3 text-sm text-muted">
          Create a lease before recording a payment.
        </p>
      ) : null}

      {payments.length === 0 ? (
        <EmptyState title="No payments yet" body="Record the first receipt once a lease exists." />
      ) : filtered.length === 0 ? (
        <EmptyState title="No payments in this status" body="Choose another status to see the rest of the ledger." />
      ) : (
        <div className="panel overflow-hidden">
          <div className="overflow-x-auto">
            <table className="data-table w-full min-w-[800px] text-left text-sm">
              <thead className="text-muted">
                <tr>
                  <th className="px-4 py-3 font-medium">Date</th>
                  <th className="px-4 py-3 font-medium">Lease</th>
                  <th className="px-4 py-3 font-medium">Method</th>
                  <th className="px-4 py-3 text-right font-medium">Amount</th>
                  <th className="px-4 py-3 font-medium">Status</th>
                  <th className="px-4 py-3 text-right font-medium">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {filtered.map((payment) => {
                  const lease = leases.find((item) => item.leaseId === payment.leaseId);
                  const property = properties.find((item) => item.propertyId === lease?.propertyId);
                  return (
                    <tr key={payment.paymentId} className="hover:bg-paper/70">
                      <td className="px-4 py-3">
                        <p className="font-medium">{formatDate(payment.paymentDate)}</p>
                        <p className="text-xs text-muted">{payment.paymentId}</p>
                      </td>
                      <td className="px-4 py-3">
                        <p>{lease?.tenantName ?? "Unknown tenant"}</p>
                        <p className="text-xs text-muted">{property?.address ?? payment.leaseId}</p>
                      </td>
                      <td className="px-4 py-3">{payment.paymentMethod}</td>
                      <td className="num px-4 py-3 text-right font-medium">{formatZar(payment.amount)}</td>
                      <td className="px-4 py-3">
                        <StatusBadge status={payment.status} />
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex justify-end gap-1">
                          <Button
                            variant="ghost"
                            size="icon"
                            aria-label={`Edit payment ${payment.paymentId}`}
                            onClick={() => {
                              setEditing(payment);
                              setOpen(true);
                            }}
                          >
                            <Pencil size={16} />
                          </Button>
                          <Button
                            variant="ghost"
                            size="icon"
                            aria-label={`Remove payment ${payment.paymentId}`}
                            onClick={() => setRemoving(payment)}
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
        title={editing ? "Edit payment" : "Record payment"}
        description="Amount, date, method, and whether it is paid, pending, or overdue."
        onClose={() => setOpen(false)}
      >
        {open ? (
          <PaymentForm
            key={editing?.paymentId ?? "new"}
            payment={editing}
            onClose={() => setOpen(false)}
            onSave={savePayment}
          />
        ) : null}
      </Modal>

      <ConfirmDialog
        open={removing !== null}
        title="Remove payment"
        body={removing ? `Remove the ${formatZar(removing.amount)} payment recorded on ${formatDate(removing.paymentDate)}?` : ""}
        confirmLabel="Remove payment"
        busy={busy}
        onConfirm={() => void confirmRemove()}
        onClose={() => setRemoving(null)}
      />
    </div>
  );
}

function PaymentForm({
  payment,
  onClose,
  onSave,
}: {
  payment: Payment | null;
  onClose: () => void;
  onSave: (payment: Payment, mode: "create" | "update") => Promise<boolean>;
}) {
  const { leases, properties } = usePortfolio();
  const form = useForm<PaymentFormValues>({
    resolver: zodResolver(paymentSchema),
    defaultValues: {
      leaseId: payment?.leaseId ?? "",
      amount: payment ? String(payment.amount) : "",
      paymentDate: payment?.paymentDate || todayIso(),
      paymentMethod: payment?.paymentMethod ?? "EFT",
      status: payment?.status ?? "PAID",
    },
  });

  const onSubmit = form.handleSubmit(async (values) => {
    const entity: Payment = {
      paymentId: payment?.paymentId ?? createId("PAY"),
      leaseId: values.leaseId,
      amount: parseMoney(values.amount),
      paymentDate: values.paymentDate,
      paymentMethod: values.paymentMethod,
      status: values.status,
    };
    const ok = await onSave(entity, payment ? "update" : "create");
    if (ok) onClose();
  });

  return (
    <form noValidate onSubmit={onSubmit} className="space-y-4">
      <Field label="Lease" htmlFor="payment-lease" error={form.formState.errors.leaseId?.message}>
        <select id="payment-lease" className={controlClass} {...form.register("leaseId")}>
          <option value="">Select a lease</option>
          {leases.map((lease) => {
            const property = properties.find((item) => item.propertyId === lease.propertyId);
            return (
              <option key={lease.leaseId} value={lease.leaseId}>
                {lease.tenantName} · {property?.address ?? lease.propertyId}
              </option>
            );
          })}
        </select>
      </Field>
      <div className="grid gap-4 sm:grid-cols-2">
        <Field label="Amount" htmlFor="payment-amount" error={form.formState.errors.amount?.message}>
          <input id="payment-amount" inputMode="decimal" className={controlClass} {...form.register("amount")} />
        </Field>
        <Field label="Payment date" htmlFor="payment-date" error={form.formState.errors.paymentDate?.message}>
          <input id="payment-date" type="date" className={controlClass} {...form.register("paymentDate")} />
        </Field>
      </div>
      <div className="grid gap-4 sm:grid-cols-2">
        <Field label="Method" htmlFor="payment-method" error={form.formState.errors.paymentMethod?.message}>
          <select id="payment-method" className={controlClass} {...form.register("paymentMethod")}>
            {METHODS.map((method) => (
              <option key={method} value={method}>
                {method}
              </option>
            ))}
          </select>
        </Field>
        <Field label="Status" htmlFor="payment-status" error={form.formState.errors.status?.message}>
          <select id="payment-status" className={controlClass} {...form.register("status")}>
            <option value="PAID">Paid</option>
            <option value="PENDING">Pending</option>
            <option value="OVERDUE">Overdue</option>
          </select>
        </Field>
      </div>
      <div className="flex flex-wrap justify-end gap-2 pt-2">
        <Button variant="secondary" onClick={onClose}>
          Cancel
        </Button>
        <Button type="submit" disabled={form.formState.isSubmitting || leases.length === 0}>
          {form.formState.isSubmitting ? "Saving…" : payment ? "Save changes" : "Record payment"}
        </Button>
      </div>
    </form>
  );
}
