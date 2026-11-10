import { useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Pencil, Plus, Trash2 } from "lucide-react";
import { z } from "zod";
import { usePortfolio } from "../context/PortfolioContext.tsx";
import { fullName } from "../lib/format.ts";
import { createId } from "../lib/ids.ts";
import type { Tenant } from "../types/index.ts";
import { PageIntro } from "../components/layout/PageIntro.tsx";
import { Button } from "../components/ui/Button.tsx";
import { ConfirmDialog } from "../components/ui/ConfirmDialog.tsx";
import { EmptyState } from "../components/ui/EmptyState.tsx";
import { controlClass, Field } from "../components/ui/Field.tsx";
import { Modal } from "../components/ui/Modal.tsx";
import { SearchField } from "../components/ui/SearchField.tsx";

const tenantSchema = z.object({
  firstName: z.string().trim().min(1, "Enter a first name").max(80, "First name is too long"),
  lastName: z.string().trim().min(1, "Enter a last name").max(80, "Last name is too long"),
  email: z.string().trim().email("Enter a valid email address"),
  phoneNumber: z.string().trim().min(7, "Enter a phone number").max(20, "Phone number is too long"),
});

type TenantFormValues = z.infer<typeof tenantSchema>;

export function TenantsPage() {
  const { tenants, saveTenant, deleteTenant } = usePortfolio();
  const [query, setQuery] = useState("");
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Tenant | null>(null);
  const [removing, setRemoving] = useState<Tenant | null>(null);
  const [busy, setBusy] = useState(false);

  const filtered = useMemo(() => {
    const needle = query.trim().toLowerCase();
    return [...tenants]
      .filter((tenant) => {
        if (!needle) return true;
        return [fullName(tenant), tenant.email, tenant.phoneNumber, tenant.tenantId].some((value) =>
          value.toLowerCase().includes(needle),
        );
      })
      .sort((a, b) => a.lastName.localeCompare(b.lastName) || a.firstName.localeCompare(b.firstName));
  }, [tenants, query]);

  async function confirmRemove() {
    if (!removing) return;
    setBusy(true);
    const ok = await deleteTenant(removing.tenantId);
    setBusy(false);
    if (ok) setRemoving(null);
  }

  return (
    <div>
      <PageIntro description="Tenant profiles and the contact details used when a lease is drawn up.">
        <SearchField
          id="tenant-search"
          label="Search tenants"
          value={query}
          onChange={setQuery}
          placeholder="Search name, email, or phone"
        />
        <Button
          onClick={() => {
            setEditing(null);
            setOpen(true);
          }}
        >
          <Plus size={16} /> Add tenant
        </Button>
      </PageIntro>

      {tenants.length === 0 ? (
        <EmptyState
          title="No tenants yet"
          body="Add a name, email, and phone number to start a tenant file."
          action={
            <Button
              onClick={() => {
                setEditing(null);
                setOpen(true);
              }}
            >
              <Plus size={16} /> Add tenant
            </Button>
          }
        />
      ) : filtered.length === 0 ? (
        <EmptyState title="No matching tenants" body="Try a different name, email, or phone number." />
      ) : (
        <div className="panel overflow-hidden">
          <div className="overflow-x-auto">
            <table className="data-table w-full min-w-[720px] text-left text-sm">
              <thead className="text-muted">
                <tr>
                  <th className="px-4 py-3 font-medium">Name</th>
                  <th className="px-4 py-3 font-medium">Email</th>
                  <th className="px-4 py-3 font-medium">Phone</th>
                  <th className="px-4 py-3 font-medium">Id</th>
                  <th className="px-4 py-3 text-right font-medium">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {filtered.map((tenant) => (
                  <tr key={tenant.tenantId} className="hover:bg-paper/70">
                    <td className="px-4 py-3 font-medium">{fullName(tenant)}</td>
                    <td className="px-4 py-3">
                      <a className="font-medium text-teal-deep underline-offset-2 hover:text-teal hover:underline" href={`mailto:${tenant.email}`}>
                        {tenant.email}
                      </a>
                    </td>
                    <td className="px-4 py-3">
                      <a className="underline-offset-2 hover:text-teal-deep hover:underline" href={`tel:${tenant.phoneNumber.replace(/\s/g, "")}`}>
                        {tenant.phoneNumber}
                      </a>
                    </td>
                    <td className="px-4 py-3 text-muted">{tenant.tenantId}</td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-1">
                        <Button
                          variant="ghost"
                          size="icon"
                          aria-label={`Edit ${fullName(tenant)}`}
                          onClick={() => {
                            setEditing(tenant);
                            setOpen(true);
                          }}
                        >
                          <Pencil size={16} />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          aria-label={`Remove ${fullName(tenant)}`}
                          onClick={() => setRemoving(tenant)}
                        >
                          <Trash2 size={16} />
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <Modal
        open={open}
        title={editing ? "Edit tenant" : "Add tenant"}
        description="Name and contact details for the tenant file."
        onClose={() => setOpen(false)}
      >
        {open ? (
          <TenantForm key={editing?.tenantId ?? "new"} tenant={editing} onClose={() => setOpen(false)} onSave={saveTenant} />
        ) : null}
      </Modal>

      <ConfirmDialog
        open={removing !== null}
        title="Remove tenant"
        body={removing ? `Remove ${fullName(removing)} from the tenant list? Existing leases keep the saved name.` : ""}
        confirmLabel="Remove tenant"
        busy={busy}
        onConfirm={() => void confirmRemove()}
        onClose={() => setRemoving(null)}
      />
    </div>
  );
}

function TenantForm({
  tenant,
  onClose,
  onSave,
}: {
  tenant: Tenant | null;
  onClose: () => void;
  onSave: (tenant: Tenant, mode: "create" | "update") => Promise<boolean>;
}) {
  const form = useForm<TenantFormValues>({
    resolver: zodResolver(tenantSchema),
    defaultValues: {
      firstName: tenant?.firstName ?? "",
      lastName: tenant?.lastName ?? "",
      email: tenant?.email ?? "",
      phoneNumber: tenant?.phoneNumber ?? "",
    },
  });

  const onSubmit = form.handleSubmit(async (values) => {
    const entity: Tenant = {
      tenantId: tenant?.tenantId ?? createId("TNT"),
      firstName: values.firstName,
      lastName: values.lastName,
      email: values.email,
      phoneNumber: values.phoneNumber,
    };
    const ok = await onSave(entity, tenant ? "update" : "create");
    if (ok) onClose();
  });

  return (
    <form noValidate onSubmit={onSubmit} className="space-y-4">
      <div className="grid gap-4 sm:grid-cols-2">
        <Field label="First name" htmlFor="tenant-first" error={form.formState.errors.firstName?.message}>
          <input id="tenant-first" className={controlClass} {...form.register("firstName")} />
        </Field>
        <Field label="Last name" htmlFor="tenant-last" error={form.formState.errors.lastName?.message}>
          <input id="tenant-last" className={controlClass} {...form.register("lastName")} />
        </Field>
      </div>
      <Field label="Email" htmlFor="tenant-email" error={form.formState.errors.email?.message}>
        <input id="tenant-email" type="email" className={controlClass} {...form.register("email")} />
      </Field>
      <Field label="Phone" htmlFor="tenant-phone" error={form.formState.errors.phoneNumber?.message}>
        <input id="tenant-phone" className={controlClass} {...form.register("phoneNumber")} />
      </Field>
      <div className="flex justify-end gap-2 pt-2">
        <Button variant="secondary" onClick={onClose}>
          Cancel
        </Button>
        <Button type="submit" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Saving…" : tenant ? "Save changes" : "Add tenant"}
        </Button>
      </div>
    </form>
  );
}
