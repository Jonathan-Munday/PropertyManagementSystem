import { useEffect, useState } from "react";
import { NavLink } from "react-router-dom";
import { Building2, FileText, LayoutDashboard, UserPlus, Users, Wallet, Wrench } from "lucide-react";
import { clsx } from "clsx";
import { useAuth } from "../../context/AuthContext.tsx";
import { roleLabel } from "../../lib/permissions.ts";
import { usePortfolio } from "../../context/PortfolioContext.tsx";
import { BrandLockup } from "../brand/BrandMark.tsx";

const links = [
  { to: "/", label: "Overview", icon: LayoutDashboard, end: true, countKey: null },
  { to: "/properties", label: "Properties", icon: Building2, end: false, countKey: "properties" },
  { to: "/tenants", label: "Tenants", icon: Users, end: false, countKey: "tenants" },
  { to: "/leases", label: "Leases", icon: FileText, end: false, countKey: "leases" },
  { to: "/payments", label: "Payments", icon: Wallet, end: false, countKey: "payments" },
  { to: "/maintenance", label: "Maintenance", icon: Wrench, end: false, countKey: "maintenance" },
  { to: "/team", label: "Team", icon: UserPlus, end: false, countKey: null },
] as const;

function useDesktopNav() {
  const query = "(min-width: 1024px)";
  const [desktop, setDesktop] = useState(() => window.matchMedia(query).matches);

  useEffect(() => {
    const media = window.matchMedia(query);
    const onChange = () => setDesktop(media.matches);
    media.addEventListener("change", onChange);
    return () => media.removeEventListener("change", onChange);
  }, []);

  return desktop;
}

export function Sidebar({ open, onClose }: { open: boolean; onClose: () => void }) {
  const portfolio = usePortfolio();
  const auth = useAuth();
  const desktop = useDesktopNav();
  const counts: Record<string, number> = {
    properties: portfolio.properties.length,
    tenants: portfolio.tenants.length,
    leases: portfolio.leases.length,
    payments: portfolio.payments.length,
    maintenance: portfolio.maintenance.filter((item) => item.status !== "RESOLVED").length,
  };
  const organisation = auth.selectedOrganisation;

  useEffect(() => {
    if (desktop || !open) return;
    const previous = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = previous;
    };
  }, [desktop, open]);

  return (
    <>
      {open ? (
        <button
          type="button"
          className="fixed inset-0 z-30 bg-navy-deep/50 lg:hidden"
          aria-label="Close menu"
          onClick={onClose}
        />
      ) : null}
      <aside
        inert={!desktop && !open ? true : undefined}
        className={clsx(
          "fixed inset-y-0 left-0 z-40 flex w-[17.5rem] flex-col bg-navy-deep text-porcelain motion-safe:transition-transform motion-safe:duration-200 lg:translate-x-0",
          open ? "translate-x-0" : "-translate-x-full",
        )}
      >
        <div className="px-5 pt-5 pb-4">
          <BrandLockup tone="onDark" />
        </div>
        <div className="mx-5 h-px bg-teal/80" />
        <nav className="flex-1 space-y-1 overflow-y-auto px-3 py-4" aria-label="Primary">
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
                    "flex items-center justify-between gap-3 rounded-lg px-3 py-2.5 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-teal focus-visible:ring-offset-2 focus-visible:ring-offset-navy-deep",
                    isActive ? "bg-white/10 text-white" : "text-white/70 hover:bg-white/5 hover:text-white",
                  )
                }
              >
                {({ isActive }) => (
                  <>
                    <span className="flex min-w-0 items-center gap-3">
                      <span className={clsx("h-4 w-0.5 shrink-0 rounded-full", isActive ? "bg-teal" : "bg-transparent")} />
                      <Icon size={18} aria-hidden="true" />
                      <span className="truncate">{link.label}</span>
                    </span>
                    {count !== null ? (
                      <span className={clsx("num text-xs", isActive ? "text-white" : "text-white/45")}>{count}</span>
                    ) : null}
                  </>
                )}
              </NavLink>
            );
          })}
        </nav>
        <div className="border-t border-white/10 px-5 py-4">
          <p className="truncate text-sm font-medium text-white">{organisation?.organisationName ?? "Property desk"}</p>
          <p className="mt-0.5 text-xs text-white/55">
            {organisation ? roleLabel(organisation.role) : "No organisation selected"}
          </p>
        </div>
      </aside>
    </>
  );
}
