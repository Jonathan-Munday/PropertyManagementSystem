import { useEffect } from "react";
import { useLocation } from "react-router-dom";
import { Menu } from "lucide-react";
import { clsx } from "clsx";
import { useAuth } from "../../context/AuthContext.tsx";
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
  "/team": "Team",
  "/subscription": "Subscription",
};

export function Topbar({ onMenu }: { onMenu: () => void }) {
  const { pathname } = useLocation();
  const { source } = usePortfolio();
  const auth = useAuth();
  const title = titles[pathname] ?? "Overview";
  const connected = source === "api";

  useEffect(() => {
    document.title = `${title} · Portico`;
  }, [title]);

  return (
    <header className="sticky top-0 z-20 border-b border-line bg-paper">
      <div className="flex flex-col gap-3 px-4 py-3 sm:flex-row sm:items-center sm:justify-between sm:px-6 lg:px-8">
        <div className="flex min-w-0 items-center gap-3">
          <Button variant="ghost" size="icon" className="shrink-0 lg:hidden" onClick={onMenu} aria-label="Open menu">
            <Menu size={18} />
          </Button>
          <div className="min-w-0">
            <p className="text-xs text-muted">{formatLongDate()}</p>
            <h1 className="truncate font-display text-xl leading-tight sm:text-2xl">{title}</h1>
          </div>
        </div>
        <div className="flex flex-wrap items-center gap-2 sm:justify-end">
          {auth.organisations.length > 1 ? (
            <label className="sr-only" htmlFor="organisation">
              Organisation
            </label>
          ) : null}
          {auth.organisations.length > 1 ? (
            <select
              id="organisation"
              className="h-8 max-w-full rounded-lg border border-line bg-card px-2 text-xs text-ink outline-none focus:border-teal focus:ring-2 focus:ring-teal/20 sm:max-w-48"
              value={auth.selectedOrganisationId ?? ""}
              onChange={(event) => auth.selectOrganisation(event.target.value)}
            >
              {auth.organisations.map((organisation) => (
                <option key={organisation.organisationId} value={organisation.organisationId}>
                  {organisation.organisationName}
                </option>
              ))}
            </select>
          ) : null}
          <span
            className={clsx(
              "inline-flex h-8 items-center gap-2 rounded-full px-3 text-xs font-medium",
              connected ? "bg-moss-soft text-moss" : "bg-amber-soft text-amber",
            )}
          >
            <span className={clsx("h-1.5 w-1.5 rounded-full", connected ? "bg-moss" : "bg-amber")} />
            {connected ? "API connected" : "Sample data"}
          </span>
          <Button variant="secondary" size="sm" onClick={() => void auth.signOut()}>
            Sign out
          </Button>
        </div>
      </div>
    </header>
  );
}
