import { useState, type ReactNode } from "react";
import { useLocation } from "react-router-dom";
import { usePortfolio } from "../../context/PortfolioContext.tsx";
import { BrandLockup } from "../brand/BrandMark.tsx";
import { Button } from "../ui/Button.tsx";
import { Sidebar } from "./Sidebar.tsx";
import { Topbar } from "./Topbar.tsx";

export function AppShell({ children }: { children: ReactNode }) {
  const { pathname } = useLocation();
  const { loading, source, notice, dismissNotice, refreshing, retryConnection, resetDemo } = usePortfolio();
  const [menuPath, setMenuPath] = useState<string | null>(null);
  const [banner, setBanner] = useState(true);
  const open = menuPath === pathname;

  if (loading) {
    return (
      <div className="grid min-h-screen place-items-center bg-paper px-6 text-center">
        <div>
          <BrandLockup />
          <p className="mt-4 text-sm text-muted">Connecting to your portfolio…</p>
          <span className="mx-auto mt-5 block h-2 w-2 animate-pulse rounded-full bg-teal" />
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-paper text-ink">
      <Sidebar open={open} onClose={() => setMenuPath(null)} />
      <div className="min-w-0 lg:pl-[17.5rem]">
        <Topbar onMenu={() => setMenuPath(pathname)} />
        {source === "demo" && banner ? (
          <div className="border-b border-amber/20 bg-amber-soft px-4 py-3 text-sm text-ink sm:px-6 lg:px-8">
            <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
              <p className="max-w-3xl leading-6">
                The property service did not respond. You are editing the Cape Town sample portfolio kept in this browser.
              </p>
              <div className="flex flex-wrap gap-2">
                <Button variant="secondary" size="sm" onClick={() => void retryConnection()} disabled={refreshing}>
                  {refreshing ? "Checking…" : "Try API again"}
                </Button>
                <Button variant="secondary" size="sm" onClick={resetDemo}>
                  Reset sample
                </Button>
                <Button variant="ghost" size="sm" onClick={() => setBanner(false)}>
                  Dismiss
                </Button>
              </div>
            </div>
          </div>
        ) : null}
        <main className="px-4 py-6 sm:px-6 lg:px-8">{children}</main>
      </div>
      {notice ? (
        <div
          className="fixed right-4 bottom-4 z-50 flex max-w-sm items-start gap-3 rounded-xl border border-white/10 bg-navy-deep px-4 py-3 text-sm text-porcelain shadow-lg"
          role="status"
        >
          <p className="leading-5">{notice}</p>
          <button type="button" className="text-white/70 hover:text-white" onClick={dismissNotice} aria-label="Dismiss notice">
            ×
          </button>
        </div>
      ) : null}
    </div>
  );
}
