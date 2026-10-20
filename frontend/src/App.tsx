import { Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "./components/layout/AppShell.tsx";
import { DashboardPage } from "./pages/DashboardPage.tsx";
import { LeasesPage } from "./pages/LeasesPage.tsx";
import { MaintenancePage } from "./pages/MaintenancePage.tsx";
import { PaymentsPage } from "./pages/PaymentsPage.tsx";
import { PropertiesPage } from "./pages/PropertiesPage.tsx";
import { TenantsPage } from "./pages/TenantsPage.tsx";

export default function App() {
  return (
    <AppShell>
      <Routes>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/properties" element={<PropertiesPage />} />
        <Route path="/tenants" element={<TenantsPage />} />
        <Route path="/leases" element={<LeasesPage />} />
        <Route path="/payments" element={<PaymentsPage />} />
        <Route path="/maintenance" element={<MaintenancePage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AppShell>
  );
}
