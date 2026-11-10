import { clsx } from "clsx";

const tones: Record<string, string> = {
  PAID: "bg-moss-soft text-moss",
  PENDING: "bg-amber-soft text-amber",
  OVERDUE: "bg-clay-soft text-clay",
  OPEN: "bg-sea-soft text-sea",
  IN_PROGRESS: "bg-amber-soft text-amber",
  RESOLVED: "bg-moss-soft text-moss",
  Available: "bg-moss-soft text-moss",
  Occupied: "bg-navy-soft text-navy",
  Active: "bg-moss-soft text-moss",
  Ended: "border border-line bg-paper text-muted",
  Upcoming: "bg-sea-soft text-sea",
  "Needs dates": "bg-amber-soft text-amber",
};

const labels: Record<string, string> = {
  PAID: "Paid",
  PENDING: "Pending",
  OVERDUE: "Overdue",
  OPEN: "Open",
  IN_PROGRESS: "In progress",
  RESOLVED: "Resolved",
};

export function StatusBadge({ status }: { status: string }) {
  return (
    <span
      className={clsx(
        "inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium whitespace-nowrap",
        tones[status] ?? "bg-paper text-muted",
      )}
    >
      {labels[status] ?? status}
    </span>
  );
}
