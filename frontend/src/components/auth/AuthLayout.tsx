import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import { supabaseConfigError } from "../../lib/supabase.ts";
import { BrandLockup } from "../brand/BrandMark.tsx";

export function AuthLayout({
  title,
  lede,
  children,
  alternate,
}: {
  title: string;
  lede: string;
  children: ReactNode;
  alternate?: ReactNode;
}) {
  return (
    <div className="grid min-h-screen lg:grid-cols-[minmax(18rem,0.86fr)_minmax(0,1.14fr)]">
      <aside className="hidden flex-col justify-between bg-navy-deep px-10 py-12 text-porcelain lg:flex">
        <BrandLockup tone="onDark" />
        <div className="max-w-sm">
          <p className="font-display text-4xl leading-tight text-porcelain">The portfolio, kept in order.</p>
          <p className="mt-4 text-sm leading-6 text-white/65">
            Properties, tenants, leases, rent and maintenance for the organisation you manage.
          </p>
        </div>
      </aside>
      <div className="grid place-items-center bg-paper px-4 py-10 sm:px-6">
        <div className="w-full max-w-md">
          <Link to="/login" className="inline-flex lg:hidden">
            <BrandLockup />
          </Link>
          <h1 className="mt-8 font-display text-4xl leading-tight lg:mt-0">{title}</h1>
          <p className="mt-3 text-sm leading-6 text-muted">{lede}</p>
          <div className="panel mt-8 p-5 sm:p-6">
            {supabaseConfigError ? (
              <p className="mb-4 rounded-lg bg-amber-soft px-3 py-2 text-sm leading-5 text-amber" role="status">
                {supabaseConfigError} Check the frontend environment settings, then restart the app.
              </p>
            ) : null}
            {children}
          </div>
          {alternate ? <div className="mt-4 text-sm text-muted">{alternate}</div> : null}
        </div>
      </div>
    </div>
  );
}

export function SessionScreen({ label }: { label: string }) {
  return (
    <div className="grid min-h-screen place-items-center bg-paper px-6 text-center">
      <div>
        <BrandLockup />
        <p className="mt-4 text-sm text-muted">{label}</p>
        <span className="mx-auto mt-5 block h-2 w-2 animate-pulse rounded-full bg-teal" />
      </div>
    </div>
  );
}

export function authErrorMessage(error: unknown) {
  if (error instanceof Error && error.message.trim()) {
    const message = error.message.toLowerCase();
    if (message.includes("invalid login") || message.includes("invalid credentials")) {
      return "Email or password is incorrect.";
    }
    if (message.includes("already registered") || message.includes("already been registered")) {
      return "An account with that email already exists. Sign in instead.";
    }
    return error.message;
  }
  return "The request could not be completed.";
}
