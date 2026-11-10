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
    <article className="panel flex h-full min-h-36 flex-col p-5 transition-colors hover:border-teal/35">
      <div className="flex items-start justify-between gap-3">
        <p className="text-sm font-medium text-muted">{label}</p>
        <span className={clsx("grid h-10 w-10 shrink-0 place-items-center rounded-lg", iconClass)}>{icon}</span>
      </div>
      <p className="num mt-4 font-display text-3xl leading-none tracking-tight">{value}</p>
      <p className="mt-2 text-sm leading-5 text-muted">{detail}</p>
    </article>
  );
}
