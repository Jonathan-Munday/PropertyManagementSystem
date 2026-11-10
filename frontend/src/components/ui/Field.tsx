import type { ReactNode } from "react";
import { clsx } from "clsx";

export const controlClass =
  "h-10 w-full rounded-lg border border-line bg-card px-3 text-sm text-ink outline-none transition placeholder:text-muted focus:border-teal focus:ring-2 focus:ring-teal/20 disabled:cursor-not-allowed disabled:bg-paper disabled:opacity-70";

export const textareaClass =
  "min-h-24 w-full rounded-lg border border-line bg-card px-3 py-2.5 text-sm text-ink outline-none transition placeholder:text-muted focus:border-teal focus:ring-2 focus:ring-teal/20 disabled:cursor-not-allowed disabled:bg-paper disabled:opacity-70";

export function choiceClass(selected: boolean, compact = false) {
  return clsx(
    "cursor-pointer rounded-lg border text-center transition has-[:focus-visible]:ring-2 has-[:focus-visible]:ring-teal has-[:focus-visible]:ring-offset-2",
    compact ? "px-2 py-2.5 text-xs sm:text-sm" : "px-3 py-2.5 text-sm",
    selected ? "border-teal bg-teal text-white" : "border-line bg-card text-ink hover:border-teal/45",
  );
}

export function segmentClass(selected: boolean) {
  return clsx(
    "inline-flex h-8 items-center gap-1.5 rounded-md px-3 text-xs font-medium transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-teal",
    selected ? "bg-teal text-white" : "text-muted hover:text-ink",
  );
}

export function Field({
  label,
  htmlFor,
  error,
  hint,
  children,
}: {
  label: string;
  htmlFor?: string;
  error?: string;
  hint?: string;
  children: ReactNode;
}) {
  return (
    <div>
      <label htmlFor={htmlFor} className="mb-1.5 block text-sm font-medium text-ink">
        {label}
      </label>
      {children}
      {error ? (
        <p className="mt-1 text-xs text-clay" role="alert">
          {error}
        </p>
      ) : hint ? (
        <p className="mt-1 text-xs text-muted">{hint}</p>
      ) : null}
    </div>
  );
}
