import { useEffect, useId, useRef, type ReactNode } from "react";
import { createPortal } from "react-dom";
import { X } from "lucide-react";
import { clsx } from "clsx";
import { Button } from "./Button.tsx";

export function Modal({
  open,
  title,
  description,
  onClose,
  children,
  footer,
  widthClass = "max-w-xl",
}: {
  open: boolean;
  title: string;
  description?: string;
  onClose: () => void;
  children?: ReactNode;
  footer?: ReactNode;
  widthClass?: string;
}) {
  const titleId = useId();
  const panelRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;
    const onKey = (event: KeyboardEvent) => {
      if (event.key === "Escape") onClose();
    };
    const previous = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    window.addEventListener("keydown", onKey);
    panelRef.current?.focus();
    return () => {
      document.body.style.overflow = previous;
      window.removeEventListener("keydown", onKey);
    };
  }, [open, onClose]);

  if (!open) return null;

  return createPortal(
    <div className="fixed inset-0 z-50 flex items-end justify-center p-3 sm:items-center sm:p-6">
      <div className="absolute inset-0 bg-ink/50" onClick={onClose} aria-hidden="true" />
      <div
        ref={panelRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        tabIndex={-1}
        className={clsx(
          "relative z-10 max-h-[92vh] w-full overflow-y-auto rounded-2xl border border-line bg-card shadow-xl outline-none",
          widthClass,
        )}
      >
        <div className="flex items-start justify-between gap-4 border-b border-line px-5 py-4">
          <div>
            <h2 id={titleId} className="font-display text-2xl leading-tight">
              {title}
            </h2>
            {description ? <p className="mt-1 text-sm leading-6 text-muted">{description}</p> : null}
          </div>
          <Button variant="ghost" size="icon" onClick={onClose} aria-label="Close dialog">
            <X size={18} />
          </Button>
        </div>
        {children ? <div className="px-5 py-4">{children}</div> : null}
        {footer ? <div className="flex flex-wrap justify-end gap-2 border-t border-line px-5 py-4">{footer}</div> : null}
      </div>
    </div>,
    document.body,
  );
}
