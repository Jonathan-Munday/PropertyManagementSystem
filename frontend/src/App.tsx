import { Navigate, Outlet, Route, Routes, useLocation } from "react-router-dom";
import { AppShell } from "./components/layout/AppShell.tsx";
import { AuthLayout, SessionScreen } from "./components/auth/AuthLayout.tsx";
import { Button } from "./components/ui/Button.tsx";
import { AuthProvider, useAuth } from "./context/AuthContext.tsx";
import { PortfolioProvider } from "./context/PortfolioContext.tsx";
import { AcceptInvitePage } from "./pages/auth/AcceptInvitePage.tsx";
import { ForgotPasswordPage } from "./pages/auth/ForgotPasswordPage.tsx";
import { LoginPage } from "./pages/auth/LoginPage.tsx";
import { OnboardingPage } from "./pages/auth/OnboardingPage.tsx";
import { RegisterPage } from "./pages/auth/RegisterPage.tsx";
import { ResetPasswordPage } from "./pages/auth/ResetPasswordPage.tsx";
import { VerifyEmailPage } from "./pages/auth/VerifyEmailPage.tsx";
import { DashboardPage } from "./pages/DashboardPage.tsx";
import { LeasesPage } from "./pages/LeasesPage.tsx";
import { MaintenancePage } from "./pages/MaintenancePage.tsx";
import { PaymentsPage } from "./pages/PaymentsPage.tsx";
import { PropertiesPage } from "./pages/PropertiesPage.tsx";
import { TeamPage } from "./pages/TeamPage.tsx";
import { TenantsPage } from "./pages/TenantsPage.tsx";

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="/verify-email" element={<VerifyEmailPage />} />
        <Route path="/onboarding" element={<OnboardingPage />} />
        <Route path="/invite" element={<AcceptInvitePage />} />
        <Route element={<DeskGate />}>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/properties" element={<PropertiesPage />} />
          <Route path="/tenants" element={<TenantsPage />} />
          <Route path="/leases" element={<LeasesPage />} />
          <Route path="/payments" element={<PaymentsPage />} />
          <Route path="/maintenance" element={<MaintenancePage />} />
          <Route path="/team" element={<TeamPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </AuthProvider>
  );
}

function DeskGate() {
  const auth = useAuth();
  const location = useLocation();

  if (auth.loading || auth.accountState.status === "loading") {
    return <SessionScreen label="Checking your session…" />;
  }
  if (!auth.session) {
    const next = `${location.pathname}${location.search}`;
    return <Navigate to={`/login?next=${encodeURIComponent(next)}`} replace />;
  }
  if (!auth.emailVerified) return <Navigate to="/verify-email" replace />;
  if (auth.accountState.status === "error") {
    return (
      <AuthLayout title="The API is unreachable" lede={auth.accountState.message}>
        <div className="space-y-3">
          <Button type="button" className="w-full" onClick={() => void auth.refreshAccount()}>
            Try again
          </Button>
          <Button type="button" variant="secondary" className="w-full" onClick={() => void auth.signOut()}>
            Sign out
          </Button>
        </div>
      </AuthLayout>
    );
  }
  if (auth.accountState.status !== "ready") return <SessionScreen label="Loading your organisation…" />;
  if (auth.organisations.length === 0) return <Navigate to="/onboarding" replace />;
  if (!auth.selectedOrganisationId) return <SessionScreen label="Loading your organisation…" />;

  return (
    <PortfolioProvider organisationId={auth.selectedOrganisationId}>
      <AppShell>
        <Outlet />
      </AppShell>
    </PortfolioProvider>
  );
}
