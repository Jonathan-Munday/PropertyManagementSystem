import type { ReactNode } from "react";

export function PageIntro({ description, children }: { description: string; children?: ReactNode }) {
  return (
    <div className="mb-5 flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
      <p className="max-w-2xl text-sm leading-6 text-muted">{description}</p>
      {children ? <div className="flex flex-wrap items-center gap-2">{children}</div> : null}
    </div>
  );
}
