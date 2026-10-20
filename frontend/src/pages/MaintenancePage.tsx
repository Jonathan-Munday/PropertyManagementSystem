import { useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Pencil, Plus, Trash2 } from "lucide-react";
import { z } from "zod";
import { clsx } from "clsx";
import { usePortfolio } from "../context/PortfolioContext.tsx";
import { formatDate, formatZar, parseMoney, todayIso } from "../lib/format.ts";
import { createId } from "../lib/ids.ts";
import type { MaintenanceRequest, MaintenanceStatus } from "../types/index.ts";
import { PageIntro } from "../components/layout/PageIntro.tsx";
import { Button } from "../components/ui/Button.tsx";
import { ConfirmDialog } from "../components/ui/ConfirmDialog.tsx";
import { EmptyState } from "../components/ui/EmptyState.tsx";
import { controlClass, Field, textareaClass } from "../components/ui/Field.tsx";
import { Modal } from "../components/ui/Modal.tsx";
import { SearchField } from "../components/ui/SearchField.tsx";

const STATUSES = ["OPEN", "IN_PROGRESS", "RESOLVED"] as const;

const maintenanceSchema = z.object({
  description: z.string().trim().min(8, "Describe the problem in a sentence").max(500, "Keep the note under 500 characters"),
  status: z.enum(STATUSES),
  reportedDate: z.string().min(1, "Choose the date reported"),
  cost: z
    .string()
    .trim()
    .refine((value) => {
      const amount = parseMoney(value);
      return Number.isFinite(amount) && amount >= 0;
    }, "Enter a cost, or 0 if it is not known yet"),
});

type MaintenanceFormValues = z.infer<typeof maintenanceSchema>;

const columns: { status: MaintenanceStatus; title: string; hint: string }[] = [
  { status: "OPEN", title: "Open", hint: "Newly reported" },
  { status: "IN_PROGRESS", title: "In progress", hint: "Work underway" },
  { status: "RESOLVED", title: "Resolved", hint: "Closed tickets" },
];

export function MaintenancePage() {
  const { maintenance, saveMaintenance, deleteMaintenance } = usePortfolio();
  const [query, setQuery] = useState("");
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<MaintenanceRequest | null>(null);
  const [removing, setRemoving] = useState<MaintenanceRequest | null>(null);
  const [busy, setBusy] = useState(false);
  const [movingId, setMovingId] = useState<string | null>(null);

  const visible = useMemo(() => {
    const needle = query.trim().toLowerCase();
    return maintenance.filter((item) => !needle || item.description.toLowerCase().includes(needle) || item.requestId.toLowerCase().includes(needle));
  }, [maintenance, query]);

  async function move(ticket: MaintenanceRequest, status: MaintenanceStatus) {
    setMovingId(ticket.requestId);
    await saveMaintenance({ ...ticket, status }, "update");
    setMovingId(null);
  }

  async function confirmRemove() {
    if (!removing) return;
    setBusy(true);
    const ok = await deleteMaintenance(removing.requestId);
    setBusy(false);
    if (ok) setRemoving(null);
  }

  return (
    <div>
      <PageIntro description="Move a request from open, to in progress, to resolved as the work is done.">
        <SearchField
          id="maintenance-search"
          label="Search maintenance"
          value={query}
          onChange={setQuery}
          placeholder="Search requests"
        />
        <Button
          onClick={() => {
            setEditing(null);
            setOpen(true);
          }}
        >
          <Plus size={16} /> Log request
        </Button>
      </PageIntro>

      {maintenance.length === 0 ? (
        <EmptyState title="No maintenance requests" body="Log a fault, an estimate, and a status to open the board." />
      ) : visible.length === 0 ? (
        <EmptyState title="No matching requests" body="Try a different word from the ticket description." />
      ) : (
        <div className="grid gap-4 lg:grid-cols-3">
          {columns.map((column) => {
            const tickets = visible
              .filter((item) => item.status === column.status)
              .sort((a, b) => b.reportedDate.localeCompare(a.reportedDate));
            const total = tickets.reduce((sum, ticket) => sum + ticket.cost, 0);
            return (
              <section key={column.status} className="flex min-h-80 flex-col rounded-2xl border border-line bg-paper p-3">
                <header className="mb-3 flex items-start justify-between gap-3 px-1">
                  <div>
                    <h2 className="font-medium">{column.title}</h2>
                    <p className="text-xs text-muted">{column.hint}</p>
                  </div>
                  <div className="text-right">
                    <p className="num rounded-full bg-card px-2 py-0.5 text-xs">{tickets.length}</p>
                    <p className="num mt-1 text-xs text-muted">{formatZar(total)}</p>
                  </div>
                </header>
                <div className="flex flex-1 flex-col gap-3">
                  {tickets.length === 0 ? <p className="px-1 text-sm text-muted">Nothing in this column.</p> : null}
                  {tickets.map((ticket) => (
                    <article key={ticket.requestId} className="rounded-xl border border-line bg-card p-4">
                      <p className="text-sm leading-6">{ticket.description}</p>
                      <p className="mt-3 text-xs text-muted">
                        {formatDate(ticket.reportedDate)} · <span className="num font-medium text-ink">{formatZar(ticket.cost)}</span>
                      </p>
                      <div className="mt-3 flex flex-wrap gap-2">
                        {ticket.status === "IN_PROGRESS" ? (
                          <Button
                            variant="secondary"
                            size="sm"
                            disabled={movingId === ticket.requestId}
                            onClick={() => void move(ticket, "OPEN")}
                          >
                            Back to open
                          </Button>
                        ) : null}
                        {ticket.status === "RESOLVED" ? (
                          <Button
                            variant="secondary"
                            size="sm"
                            disabled={movingId === ticket.requestId}
                            onClick={() => void move(ticket, "OPEN")}
                          >
                            Reopen
                          </Button>
                        ) : null}
                        {ticket.status === "OPEN" ? (
                          <Button
                            variant="secondary"
                            size="sm"
                            disabled={movingId === ticket.requestId}
                            onClick={() => void move(ticket, "IN_PROGRESS")}
                          >
                            Start work
                          </Button>
                        ) : null}
                        {ticket.status === "IN_PROGRESS" ? (
                          <Button
                            variant="secondary"
                            size="sm"
                            disabled={movingId === ticket.requestId}
                            onClick={() => void move(ticket, "RESOLVED")}
                          >
                            Resolve
                          </Button>
                        ) : null}
                        <Button
                          variant="ghost"
                          size="sm"
                          aria-label={`Edit ${ticket.requestId}`}
                          onClick={() => {
                            setEditing(ticket);
                            setOpen(true);
                          }}
                        >
                          <Pencil size={14} /> Edit
                        </Button>
                        <Button variant="ghost" size="sm" aria-label={`Remove ${ticket.requestId}`} onClick={() => setRemoving(ticket)}>
                          <Trash2 size={14} /> Remove
                        </Button>
                      </div>
                    </article>
                  ))}
                </div>
              </section>
            );
          })}
        </div>
      )}

      <Modal
        open={open}
        title={editing ? "Edit request" : "Log request"}
        description="Describe the fault, the date it was reported, and an estimated cost."
        onClose={() => setOpen(false)}
      >
        {open ? (
          <MaintenanceForm
            key={editing?.requestId ?? "new"}
            request={editing}
            onClose={() => setOpen(false)}
            onSave={saveMaintenance}
          />
        ) : null}
      </Modal>

      <ConfirmDialog
        open={removing !== null}
        title="Remove request"
        body={removing ? "Remove this maintenance request from the board?" : ""}
        confirmLabel="Remove request"
        busy={busy}
        onConfirm={() => void confirmRemove()}
        onClose={() => setRemoving(null)}
      />
    </div>
  );
}

function MaintenanceForm({
  request,
  onClose,
  onSave,
}: {
  request: MaintenanceRequest | null;
  onClose: () => void;
  onSave: (request: MaintenanceRequest, mode: "create" | "update") => Promise<boolean>;
}) {
  const form = useForm<MaintenanceFormValues>({
    resolver: zodResolver(maintenanceSchema),
    defaultValues: {
      description: request?.description ?? "",
      status: request?.status ?? "OPEN",
      reportedDate: request?.reportedDate || todayIso(),
      cost: request ? String(request.cost) : "0",
    },
  });
  const status = form.watch("status");

  const onSubmit = form.handleSubmit(async (values) => {
    const entity: MaintenanceRequest = {
      requestId: request?.requestId ?? createId("MNT"),
      description: values.description,
      status: values.status,
      reportedDate: values.reportedDate,
      cost: parseMoney(values.cost),
    };
    const ok = await onSave(entity, request ? "update" : "create");
    if (ok) onClose();
  });

  return (
    <form noValidate onSubmit={onSubmit} className="space-y-4">
      <Field label="Description" htmlFor="maintenance-description" error={form.formState.errors.description?.message}>
        <textarea id="maintenance-description" className={textareaClass} {...form.register("description")} />
      </Field>
      <Field label="Status" error={form.formState.errors.status?.message}>
        <div className="grid grid-cols-3 gap-2">
          {(
            [
              ["OPEN", "Open"],
              ["IN_PROGRESS", "In progress"],
              ["RESOLVED", "Resolved"],
            ] as const
          ).map(([value, label]) => (
            <label
              key={value}
              className={clsx(
                "cursor-pointer rounded-lg border px-2 py-2.5 text-center text-xs sm:text-sm",
                status === value ? "border-ink bg-ink text-paper" : "border-line bg-card text-ink",
              )}
            >
              <input type="radio" className="sr-only" value={value} {...form.register("status")} />
              {label}
            </label>
          ))}
        </div>
      </Field>
      <div className="grid gap-4 sm:grid-cols-2">
        <Field label="Reported" htmlFor="maintenance-date" error={form.formState.errors.reportedDate?.message}>
          <input id="maintenance-date" type="date" className={controlClass} {...form.register("reportedDate")} />
        </Field>
        <Field label="Cost" htmlFor="maintenance-cost" error={form.formState.errors.cost?.message} hint="Use 0 if the cost is not known yet">
          <input id="maintenance-cost" inputMode="decimal" className={controlClass} {...form.register("cost")} />
        </Field>
      </div>
      <div className="flex justify-end gap-2 pt-2">
        <Button variant="secondary" onClick={onClose}>
          Cancel
        </Button>
        <Button type="submit" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Saving…" : request ? "Save changes" : "Log request"}
        </Button>
      </div>
    </form>
  );
}
