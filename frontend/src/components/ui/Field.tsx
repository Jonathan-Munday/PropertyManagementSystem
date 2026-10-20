import type { ReactNode } from "react";

export const controlClass =
  "h-10 w-full rounded-lg border border-line bg-card px-3 text-sm text-ink outline-none transition placeholder:text-muted/80 focus:border-brass focus:ring-2 focus:ring-brass/25";

export const textareaClass =
  "min-h-24 w-full rounded-lg border border-line bg-card px-3 py-2.5 text-sm text-ink outline-none transition placeholder:text-muted/80 focus:border-brass focus:ring-2 focus:ring-brass/25";

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
