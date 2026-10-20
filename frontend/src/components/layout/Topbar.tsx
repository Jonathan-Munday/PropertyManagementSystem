import { useEffect } from "react";
import { useLocation } from "react-router-dom";
import { Menu } from "lucide-react";
import { clsx } from "clsx";
import { usePortfolio } from "../../context/PortfolioContext.tsx";
import { formatLongDate } from "../../lib/format.ts";
import { Button } from "../ui/Button.tsx";

const titles: Record<string, string> = {
  "/": "Overview",
  "/properties": "Properties",
  "/tenants": "Tenants",
  "/leases": "Leases",
  "/payments": "Payments",
  "/maintenance": "Maintenance",
};

export function Topbar({ onMenu }: { onMenu: () => void }) {
  const { pathname } = useLocation();
  const { source } = usePortfolio();
  const title = titles[pathname] ?? "Overview";

  useEffect(() => {
    document.title = `${title} · Portico`;
  }, [title]);

  return (
    <header className="sticky top-0 z-20 flex items-center justify-between gap-3 border-b border-line bg-paper/90 px-4 py-3 backdrop-blur sm:px-6 lg:px-8">
      <div className="flex items-center gap-3">
        <Button variant="ghost" size="icon" className="lg:hidden" onClick={onMenu} aria-label="Open menu">
          <Menu size={18} />
        </Button>
        <div>
          <p className="text-xs text-muted">{formatLongDate()}</p>
          <h1 className="font-display text-xl leading-tight">{title}</h1>
        </div>
      </div>
      <span
        className={clsx(
          "inline-flex items-center gap-2 rounded-full px-3 py-1 text-xs font-medium",
          source === "api" ? "bg-moss-soft text-moss" : "bg-amber-soft text-amber",
        )}
      >
        <span className={clsx("h-1.5 w-1.5 rounded-full", source === "api" ? "bg-moss" : "bg-amber")} />
        {source === "api" ? "API connected" : "Sample data"}
      </span>
    </header>
  );
}
