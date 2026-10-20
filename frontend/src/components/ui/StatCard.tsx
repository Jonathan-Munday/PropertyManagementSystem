import type { ReactNode } from "react";
import { clsx } from "clsx";

export function StatCard({
  label,
  value,
  detail,
  icon,
  iconClass,
}: {
  label: string;
  value: string;
  detail: string;
  icon: ReactNode;
  iconClass: string;
}) {
  return (
    <article className="h-full rounded-2xl border border-line bg-card p-5 transition hover:-translate-y-0.5 hover:border-brass/60">
      <div className="flex items-start justify-between gap-3">
        <p className="text-sm text-muted">{label}</p>
        <span className={clsx("grid h-9 w-9 place-items-center rounded-lg", iconClass)}>{icon}</span>
      </div>
      <p className="num mt-4 font-display text-4xl tracking-tight">{value}</p>
      <p className="mt-1 text-sm text-muted">{detail}</p>
    </article>
  );
}
