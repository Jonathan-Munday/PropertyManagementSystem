import { NavLink } from "react-router-dom";
import { Building2, FileText, Landmark, LayoutDashboard, Users, Wallet, Wrench } from "lucide-react";
import { clsx } from "clsx";
import { usePortfolio } from "../../context/PortfolioContext.tsx";

const links = [
  { to: "/", label: "Overview", icon: LayoutDashboard, end: true, countKey: null },
  { to: "/properties", label: "Properties", icon: Building2, end: false, countKey: "properties" },
  { to: "/tenants", label: "Tenants", icon: Users, end: false, countKey: "tenants" },
  { to: "/leases", label: "Leases", icon: FileText, end: false, countKey: "leases" },
  { to: "/payments", label: "Payments", icon: Wallet, end: false, countKey: "payments" },
  { to: "/maintenance", label: "Maintenance", icon: Wrench, end: false, countKey: "maintenance" },
] as const;

export function Sidebar({ open, onClose }: { open: boolean; onClose: () => void }) {
  const portfolio = usePortfolio();
  const counts: Record<string, number> = {
    properties: portfolio.properties.length,
    tenants: portfolio.tenants.length,
    leases: portfolio.leases.length,
    payments: portfolio.payments.length,
    maintenance: portfolio.maintenance.filter((item) => item.status !== "RESOLVED").length,
  };

  return (
    <>
      {open ? (
        <button
          type="button"
          className="fixed inset-0 z-30 bg-ink/40 lg:hidden"
          aria-label="Close menu"
          onClick={onClose}
        />
      ) : null}
      <aside
        className={clsx(
          "fixed inset-y-0 left-0 z-40 flex w-[17.5rem] flex-col bg-ink text-paper transition-transform lg:translate-x-0",
          open ? "translate-x-0" : "-translate-x-full",
        )}
      >
        <div className="flex items-center gap-3 px-5 py-6">
          <span className="grid h-10 w-10 place-items-center rounded-xl bg-brass text-ink">
            <Landmark size={20} />
          </span>
          <div>
            <p className="font-display text-2xl leading-none">Portico</p>
            <p className="mt-1 text-xs text-white/60">Property desk</p>
          </div>
        </div>
        <nav className="flex-1 space-y-1 overflow-y-auto px-3" aria-label="Primary">
          {links.map((link) => {
            const Icon = link.icon;
            const count = link.countKey ? counts[link.countKey] : null;
            return (
              <NavLink
                key={link.to}
                to={link.to}
                end={link.end}
                onClick={onClose}
                className={({ isActive }) =>
                  clsx(
                    "flex items-center justify-between rounded-lg px-3 py-2.5 text-sm",
                    isActive ? "bg-white/10 text-white" : "text-white/70 hover:bg-white/5 hover:text-white",
                  )
                }
              >
                <span className="flex items-center gap-3">
                  <Icon size={18} aria-hidden="true" />
                  {link.label}
                </span>
                {count !== null ? <span className="num text-xs text-white/45">{count}</span> : null}
              </NavLink>
            );
          })}
        </nav>
        <p className="px-5 pt-4 pb-5 text-xs leading-5 text-white/45">Records sync with localhost:8080 when the API is up.</p>
      </aside>
    </>
  );
}
