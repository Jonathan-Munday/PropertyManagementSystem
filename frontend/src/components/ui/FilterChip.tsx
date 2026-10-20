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
        "h-8 rounded-full px-3 text-xs font-medium transition",
        pressed ? "bg-ink text-paper" : "bg-card text-muted ring-1 ring-line hover:text-ink",
      )}
    >
      {children}
    </button>
  );
}
