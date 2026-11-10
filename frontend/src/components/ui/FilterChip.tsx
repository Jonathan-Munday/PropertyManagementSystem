import type { ReactNode } from "react";
import { clsx } from "clsx";

export function FilterChip({
  pressed,
  onClick,
  children,
}: {
  pressed: boolean;
  onClick: () => void;
  children: ReactNode;
}) {
  return (
    <button
      type="button"
      aria-pressed={pressed}
      onClick={onClick}
      className={clsx(
        "h-8 rounded-full px-3 text-xs font-medium transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-teal focus-visible:ring-offset-2 focus-visible:ring-offset-paper",
        pressed ? "bg-teal text-white" : "bg-card text-muted ring-1 ring-line hover:text-ink",
      )}
    >
      {children}
    </button>
  );
}
