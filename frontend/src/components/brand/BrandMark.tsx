import { clsx } from "clsx";

/**
 * The supplied mark sits in a wide white field. The frame crops that margin
 * evenly so the emblem keeps its proportions on a dark surface.
 */
export function BrandMark({ className, decorative = false }: { className?: string; decorative?: boolean }) {
  return (
    <span
      className={clsx(
        "relative inline-block h-12 w-12 shrink-0 overflow-hidden rounded-md bg-porcelain shadow-sm ring-1 ring-navy/10",
        className,
      )}
    >
      <img
        src="/portico-logo.jpg"
        alt={decorative ? "" : "Portico"}
        className="absolute top-1/2 left-1/2 h-[152%] w-[152%] max-w-none -translate-x-1/2 -translate-y-1/2 object-cover"
        draggable={false}
      />
    </span>
  );
}

export function BrandLockup({
  tone = "onLight",
  compact = false,
}: {
  tone?: "onLight" | "onDark";
  compact?: boolean;
}) {
  const onDark = tone === "onDark";
  return (
    <div className="flex min-w-0 items-center gap-3">
      <BrandMark decorative />
      <div className="min-w-0">
        <p
          className={clsx(
            "font-display leading-none tracking-tight",
            compact ? "text-2xl" : "text-[1.7rem]",
            onDark ? "text-porcelain" : "text-navy",
          )}
        >
          Portico
        </p>
        <p className={clsx("mt-1 text-xs", onDark ? "text-white/60" : "text-muted")}>RevDev Solutions</p>
      </div>
    </div>
  );
}
