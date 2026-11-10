import { useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { LayoutGrid, List, Pencil, Plus, Trash2 } from "lucide-react";
import { z } from "zod";
import { usePortfolio } from "../context/PortfolioContext.tsx";
import { formatZar, parseMoney } from "../lib/format.ts";
import { createId } from "../lib/ids.ts";
import type { Property } from "../types/index.ts";
import { PageIntro } from "../components/layout/PageIntro.tsx";
import { Button } from "../components/ui/Button.tsx";
import { ConfirmDialog } from "../components/ui/ConfirmDialog.tsx";
import { EmptyState } from "../components/ui/EmptyState.tsx";
import { choiceClass, controlClass, Field, segmentClass, textareaClass } from "../components/ui/Field.tsx";
import { FilterChip } from "../components/ui/FilterChip.tsx";
import { Modal } from "../components/ui/Modal.tsx";
import { SearchField } from "../components/ui/SearchField.tsx";
import { StatusBadge } from "../components/ui/StatusBadge.tsx";

const propertySchema = z.object({
  address: z.string().trim().min(3, "Enter the street address"),
  description: z.string().trim().min(3, "Add a short description").max(500, "Keep the description under 500 characters"),
  rentalAmount: z
    .string()
    .trim()
    .min(1, "Enter the monthly rent")
    .refine((value) => parseMoney(value) > 0, "Monthly rent must be greater than zero"),
  status: z.enum(["available", "occupied"]),
});

type PropertyFormValues = z.infer<typeof propertySchema>;
type AvailabilityFilter = "all" | "available" | "occupied";

export function PropertiesPage() {
  const { properties, saveProperty, deleteProperty } = usePortfolio();
  const [query, setQuery] = useState("");
  const [availability, setAvailability] = useState<AvailabilityFilter>("all");
  const [view, setView] = useState<"table" | "grid">("table");
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Property | null>(null);
  const [removing, setRemoving] = useState<Property | null>(null);
  const [busy, setBusy] = useState(false);

  const filtered = useMemo(() => {
    const needle = query.trim().toLowerCase();
    return [...properties]
      .filter((property) => {
        if (availability === "available" && !property.available) return false;
        if (availability === "occupied" && property.available) return false;
        if (!needle) return true;
        return [property.address, property.description, property.propertyId].some((value) =>
          value.toLowerCase().includes(needle),
        );
      })
      .sort((a, b) => a.address.localeCompare(b.address));
  }, [properties, query, availability]);

  function startCreate() {
    setEditing(null);
    setOpen(true);
  }

  function startEdit(property: Property) {
    setEditing(property);
    setOpen(true);
  }

  async function confirmRemove() {
    if (!removing) return;
    setBusy(true);
    const ok = await deleteProperty(removing.propertyId);
    setBusy(false);
    if (ok) setRemoving(null);
  }

  return (
    <div>
      <PageIntro description="Every address on the books, with monthly rent and whether the unit is free to let.">
        <SearchField
          id="property-search"
          label="Search properties"
          value={query}
          onChange={setQuery}
          placeholder="Search address or description"
        />
        <div className="inline-flex rounded-lg border border-line bg-card p-1" role="group" aria-label="Property layout">
          <button
            type="button"
            aria-pressed={view === "table"}
            onClick={() => setView("table")}
            className={segmentClass(view === "table")}
          >
            <List size={14} /> Table
          </button>
          <button
            type="button"
            aria-pressed={view === "grid"}
            onClick={() => setView("grid")}
            className={segmentClass(view === "grid")}
          >
            <LayoutGrid size={14} /> Grid
          </button>
        </div>
        <Button onClick={startCreate}>
          <Plus size={16} /> Add property
        </Button>
      </PageIntro>

      <div className="mb-4 flex flex-wrap gap-2">
        <FilterChip pressed={availability === "all"} onClick={() => setAvailability("all")}>
          All ({properties.length})
        </FilterChip>
        <FilterChip pressed={availability === "available"} onClick={() => setAvailability("available")}>
          Available ({properties.filter((property) => property.available).length})
        </FilterChip>
        <FilterChip pressed={availability === "occupied"} onClick={() => setAvailability("occupied")}>
          Occupied ({properties.filter((property) => !property.available).length})
        </FilterChip>
      </div>

      {properties.length === 0 ? (
        <EmptyState
          title="No properties yet"
          body="Add the first address, rent, and availability to start the portfolio."
          action={
            <Button onClick={startCreate}>
              <Plus size={16} /> Add property
            </Button>
          }
        />
      ) : filtered.length === 0 ? (
        <EmptyState title="No matching properties" body="Try another address, or clear the status filter." />
      ) : view === "grid" ? (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {filtered.map((property) => (
            <article key={property.propertyId} className="flex flex-col panel p-5">
              <div className="flex items-start justify-between gap-3">
                <p className="text-xs text-muted">{property.propertyId}</p>
                <StatusBadge status={property.available ? "Available" : "Occupied"} />
              </div>
              <h2 className="mt-3 font-display text-2xl leading-tight">{property.address}</h2>
              <p className="mt-2 line-clamp-3 flex-1 text-sm leading-6 text-muted">{property.description}</p>
              <p className="num mt-5 font-display text-3xl">
                {formatZar(property.rentalAmount)}
                <span className="ml-1 font-sans text-sm text-muted">/ month</span>
              </p>
              <div className="mt-4 flex gap-2">
                <Button variant="secondary" size="sm" aria-label={`Edit ${property.address}`} onClick={() => startEdit(property)}>
                  <Pencil size={14} /> Edit
                </Button>
                <Button variant="ghost" size="sm" aria-label={`Remove ${property.address}`} onClick={() => setRemoving(property)}>
                  <Trash2 size={14} /> Remove
                </Button>
              </div>
            </article>
          ))}
        </div>
      ) : (
        <div className="overflow-hidden panel">
          <div className="overflow-x-auto">
            <table className="data-table w-full min-w-[760px] text-left text-sm">
              <thead className="text-muted">
                <tr>
                  <th className="px-4 py-3 font-medium">Address</th>
                  <th className="px-4 py-3 font-medium">Description</th>
                  <th className="px-4 py-3 text-right font-medium">Monthly rent</th>
                  <th className="px-4 py-3 font-medium">Status</th>
                  <th className="px-4 py-3 text-right font-medium">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {filtered.map((property) => (
                  <tr key={property.propertyId} className="hover:bg-paper/70">
                    <td className="px-4 py-3">
                      <p className="font-medium">{property.address}</p>
                      <p className="text-xs text-muted">{property.propertyId}</p>
                    </td>
                    <td className="max-w-xs px-4 py-3 text-muted">
                      <p className="line-clamp-2">{property.description}</p>
                    </td>
                    <td className="num px-4 py-3 text-right font-medium">{formatZar(property.rentalAmount)}</td>
                    <td className="px-4 py-3">
                      <StatusBadge status={property.available ? "Available" : "Occupied"} />
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-1">
                        <Button variant="ghost" size="icon" aria-label={`Edit ${property.address}`} onClick={() => startEdit(property)}>
                          <Pencil size={16} />
                        </Button>
                        <Button variant="ghost" size="icon" aria-label={`Remove ${property.address}`} onClick={() => setRemoving(property)}>
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
        title={editing ? "Edit property" : "Add property"}
        description="Address, monthly rent, and whether the unit is available to let."
        onClose={() => setOpen(false)}
      >
        {open ? (
          <PropertyForm
            key={editing?.propertyId ?? "new"}
            property={editing}
            onClose={() => setOpen(false)}
            onSave={saveProperty}
          />
        ) : null}
      </Modal>

      <ConfirmDialog
        open={removing !== null}
        title="Remove property"
        body={
          removing
            ? `Remove ${removing.address} from the portfolio? Leases that already point at this id will keep it.`
            : ""
        }
        confirmLabel="Remove property"
        busy={busy}
        onConfirm={() => void confirmRemove()}
        onClose={() => setRemoving(null)}
      />
    </div>
  );
}

function PropertyForm({
  property,
  onClose,
  onSave,
}: {
  property: Property | null;
  onClose: () => void;
  onSave: (property: Property, mode: "create" | "update") => Promise<boolean>;
}) {
  const form = useForm<PropertyFormValues>({
    resolver: zodResolver(propertySchema),
    defaultValues: {
      address: property?.address ?? "",
      description: property?.description ?? "",
      rentalAmount: property ? String(property.rentalAmount) : "",
      status: property ? (property.available ? "available" : "occupied") : "available",
    },
  });
  const status = form.watch("status");

  const onSubmit = form.handleSubmit(async (values) => {
    const entity: Property = {
      propertyId: property?.propertyId ?? createId("PRP"),
      address: values.address,
      description: values.description,
      rentalAmount: parseMoney(values.rentalAmount),
      available: values.status === "available",
    };
    const ok = await onSave(entity, property ? "update" : "create");
    if (ok) onClose();
  });

  return (
    <form noValidate onSubmit={onSubmit} className="space-y-4">
      <Field label="Address" htmlFor="property-address" error={form.formState.errors.address?.message}>
        <input id="property-address" className={controlClass} {...form.register("address")} />
      </Field>
      <Field label="Description" htmlFor="property-description" error={form.formState.errors.description?.message}>
        <textarea id="property-description" className={textareaClass} {...form.register("description")} />
      </Field>
      <Field
        label="Monthly rent"
        htmlFor="property-rent"
        error={form.formState.errors.rentalAmount?.message}
        hint="Rand amount, for example 12500"
      >
        <input id="property-rent" inputMode="decimal" className={controlClass} {...form.register("rentalAmount")} />
      </Field>
      <Field label="Status" error={form.formState.errors.status?.message}>
        <div className="grid grid-cols-2 gap-2">
          {(["available", "occupied"] as const).map((option) => (
            <label
              key={option}
              className={choiceClass(status === option)}
            >
              <input type="radio" className="sr-only" value={option} {...form.register("status")} />
              {option === "available" ? "Available" : "Occupied"}
            </label>
          ))}
        </div>
      </Field>
      <div className="flex flex-wrap justify-end gap-2 pt-2">
        <Button variant="secondary" onClick={onClose}>
          Cancel
        </Button>
        <Button type="submit" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Saving…" : property ? "Save changes" : "Add property"}
        </Button>
      </div>
    </form>
  );
}
