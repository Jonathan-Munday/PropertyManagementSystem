import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { demoPortfolio } from "../data/fallback.ts";
import { removeById, upsert } from "../lib/collections.ts";
import { leaseOccupies } from "../lib/format.ts";
import {
  agentApi,
  leaseApi,
  maintenanceApi,
  managerApi,
  paymentApi,
  propertyApi,
  tenantApi,
  toErrorMessage,
} from "../services/api.ts";
import type {
  Agent,
  Lease,
  MaintenanceRequest,
  Manager,
  Payment,
  Property,
  SaveMode,
  Tenant,
} from "../types/index.ts";

export type DataSource = "api" | "demo";

interface PortfolioSnapshot {
  source: DataSource;
  properties: Property[];
  tenants: Tenant[];
  leases: Lease[];
  payments: Payment[];
  maintenance: MaintenanceRequest[];
  managers: Manager[];
  agents: Agent[];
}

interface PortfolioContextValue extends PortfolioSnapshot {
  loading: boolean;
  refreshing: boolean;
  notice: string | null;
  dismissNotice: () => void;
  retryConnection: () => Promise<void>;
  resetDemo: () => void;
  saveProperty: (property: Property, mode: SaveMode) => Promise<boolean>;
  deleteProperty: (propertyId: string) => Promise<boolean>;
  saveTenant: (tenant: Tenant, mode: SaveMode) => Promise<boolean>;
  deleteTenant: (tenantId: string) => Promise<boolean>;
  saveLease: (lease: Lease, mode: SaveMode) => Promise<boolean>;
  deleteLease: (leaseId: string) => Promise<boolean>;
  savePayment: (payment: Payment, mode: SaveMode) => Promise<boolean>;
  deletePayment: (paymentId: string) => Promise<boolean>;
  saveMaintenance: (request: MaintenanceRequest, mode: SaveMode) => Promise<boolean>;
  deleteMaintenance: (requestId: string) => Promise<boolean>;
}

const PortfolioContext = createContext<PortfolioContextValue | null>(null);

function valueOrEmpty<T>(result: PromiseSettledResult<T[]>) {
  return result.status === "fulfilled" ? result.value : [];
}

function applyOccupancy(properties: Property[], propertyId: string, leases: Lease[]) {
  const occupied = leases.some((lease) => lease.propertyId === propertyId && leaseOccupies(lease));
  return properties.map((property) =>
    property.propertyId === propertyId ? { ...property, available: !occupied } : property,
  );
}

async function fetchPortfolio(): Promise<PortfolioSnapshot> {
  const [properties, tenants, leases, payments, maintenance, managers, agents] = await Promise.allSettled([
    propertyApi.list(),
    tenantApi.list(),
    leaseApi.list(),
    paymentApi.list(),
    maintenanceApi.list(),
    managerApi.list(),
    agentApi.list(),
  ]);

  if (properties.status === "rejected") {
    if (import.meta.env.VITE_DEMO_MODE === "false") {
      throw properties.reason;
    }
    return { source: "demo", ...structuredClone(demoPortfolio) };
  }

  return {
    source: "api",
    properties: properties.value,
    tenants: valueOrEmpty(tenants),
    leases: valueOrEmpty(leases),
    payments: valueOrEmpty(payments),
    maintenance: valueOrEmpty(maintenance),
    managers: valueOrEmpty(managers),
    agents: valueOrEmpty(agents),
  };
}

const emptyPortfolio: PortfolioSnapshot = {
  source: "api",
  properties: [],
  tenants: [],
  leases: [],
  payments: [],
  maintenance: [],
  managers: [],
  agents: [],
};

export function PortfolioProvider({
  organisationId,
  children,
}: {
  organisationId: string | null;
  children: ReactNode;
}) {
  const [snapshot, setSnapshot] = useState<PortfolioSnapshot>({
    source: "demo",
    properties: [],
    tenants: [],
    leases: [],
    payments: [],
    maintenance: [],
    managers: [],
    agents: [],
  });
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [notice, setNotice] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setSnapshot(emptyPortfolio);
    fetchPortfolio()
      .then((next) => {
        if (!cancelled) setSnapshot(next);
      })
      .catch((error: unknown) => {
        if (!cancelled) {
          setSnapshot(emptyPortfolio);
          setNotice(toErrorMessage(error));
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [organisationId]);

  useEffect(() => {
    if (!notice) return;
    const timeout = window.setTimeout(() => setNotice(null), 4200);
    return () => window.clearTimeout(timeout);
  }, [notice]);

  const retryConnection = useCallback(async () => {
    setRefreshing(true);
    try {
      const next = await fetchPortfolio();
      if (next.source === "demo" && snapshot.source === "api") {
        setNotice("Still unable to reach the property API. Showing the last loaded records.");
        return;
      }
      setSnapshot(next);
      setNotice(next.source === "api" ? "Connected to the property API." : "Still unable to reach the property API.");
    } catch (error) {
      setNotice(toErrorMessage(error));
    } finally {
      setRefreshing(false);
    }
  }, [snapshot.source]);

  const resetDemo = useCallback(() => {
    if (snapshot.source !== "demo") return;
    setSnapshot({ source: "demo", ...structuredClone(demoPortfolio) });
    setNotice("Sample portfolio restored.");
  }, [snapshot.source]);

  const saveProperty = useCallback(
    async (property: Property, mode: SaveMode) => {
      const commit = (saved: Property) =>
        setSnapshot((current) => ({
          ...current,
          properties: upsert(current.properties, saved, (item) => item.propertyId),
        }));
      if (snapshot.source === "demo") {
        commit(property);
        setNotice("Property saved.");
        return true;
      }
      try {
        const saved =
          mode === "create" ? await propertyApi.create(property) : await propertyApi.update(property.propertyId, property);
        commit({ ...property, ...saved, propertyId: saved.propertyId || property.propertyId });
        setNotice("Property saved.");
        return true;
      } catch (error) {
        setNotice(toErrorMessage(error));
        return false;
      }
    },
    [snapshot.source],
  );

  const deleteProperty = useCallback(
    async (propertyId: string) => {
      const commit = () =>
        setSnapshot((current) => ({
          ...current,
          properties: removeById(current.properties, propertyId, (item) => item.propertyId),
        }));
      if (snapshot.source === "demo") {
        commit();
        setNotice("Property removed.");
        return true;
      }
      try {
        await propertyApi.remove(propertyId);
        commit();
        setNotice("Property removed.");
        return true;
      } catch (error) {
        setNotice(toErrorMessage(error));
        return false;
      }
    },
    [snapshot.source],
  );

  const saveTenant = useCallback(
    async (tenant: Tenant, mode: SaveMode) => {
      const commit = (saved: Tenant) =>
        setSnapshot((current) => ({
          ...current,
          tenants: upsert(current.tenants, saved, (item) => item.tenantId),
        }));
      if (snapshot.source === "demo") {
        commit(tenant);
        setNotice("Tenant saved.");
        return true;
      }
      try {
        const saved = mode === "create" ? await tenantApi.create(tenant) : await tenantApi.update(tenant.tenantId, tenant);
        commit({ ...tenant, ...saved, tenantId: saved.tenantId || tenant.tenantId });
        setNotice("Tenant saved.");
        return true;
      } catch (error) {
        setNotice(toErrorMessage(error));
        return false;
      }
    },
    [snapshot.source],
  );

  const deleteTenant = useCallback(
    async (tenantId: string) => {
      const commit = () =>
        setSnapshot((current) => ({
          ...current,
          tenants: removeById(current.tenants, tenantId, (item) => item.tenantId),
        }));
      if (snapshot.source === "demo") {
        commit();
        setNotice("Tenant removed.");
        return true;
      }
      try {
        await tenantApi.remove(tenantId);
        commit();
        setNotice("Tenant removed.");
        return true;
      } catch (error) {
        setNotice(toErrorMessage(error));
        return false;
      }
    },
    [snapshot.source],
  );

  const saveLease = useCallback(
    async (lease: Lease, mode: SaveMode) => {
      const commit = (saved: Lease) =>
        setSnapshot((current) => {
          const nextLeases = upsert(current.leases, saved, (item) => item.leaseId);
          return {
            ...current,
            leases: nextLeases,
            properties: applyOccupancy(current.properties, saved.propertyId, nextLeases),
          };
        });
      if (snapshot.source === "demo") {
        commit(lease);
        setNotice("Lease saved.");
        return true;
      }
      try {
        const saved = mode === "create" ? await leaseApi.create(lease) : await leaseApi.update(lease.leaseId, lease);
        commit({
          ...lease,
          ...saved,
          leaseId: saved.leaseId || lease.leaseId,
          tenantId: saved.tenantId || lease.tenantId,
        });
        setNotice("Lease saved.");
        return true;
      } catch (error) {
        setNotice(toErrorMessage(error));
        return false;
      }
    },
    [snapshot.source],
  );

  const deleteLease = useCallback(
    async (leaseId: string) => {
      const commit = () =>
        setSnapshot((current) => {
          const removed = current.leases.find((item) => item.leaseId === leaseId);
          const nextLeases = removeById(current.leases, leaseId, (item) => item.leaseId);
          return {
            ...current,
            leases: nextLeases,
            properties: removed
              ? applyOccupancy(current.properties, removed.propertyId, nextLeases)
              : current.properties,
          };
        });
      if (snapshot.source === "demo") {
        commit();
        setNotice("Lease removed.");
        return true;
      }
      try {
        await leaseApi.remove(leaseId);
        commit();
        setNotice("Lease removed.");
        return true;
      } catch (error) {
        setNotice(toErrorMessage(error));
        return false;
      }
    },
    [snapshot.source],
  );

  const savePayment = useCallback(
    async (payment: Payment, mode: SaveMode) => {
      const commit = (saved: Payment) =>
        setSnapshot((current) => ({
          ...current,
          payments: upsert(current.payments, saved, (item) => item.paymentId),
        }));
      if (snapshot.source === "demo") {
        commit(payment);
        setNotice("Payment recorded.");
        return true;
      }
      try {
        const saved =
          mode === "create" ? await paymentApi.create(payment) : await paymentApi.update(payment.paymentId, payment);
        commit({
          ...payment,
          ...saved,
          paymentId: saved.paymentId || payment.paymentId,
          status: payment.status,
        });
        setNotice("Payment recorded.");
        return true;
      } catch (error) {
        setNotice(toErrorMessage(error));
        return false;
      }
    },
    [snapshot.source],
  );

  const deletePayment = useCallback(
    async (paymentId: string) => {
      const commit = () =>
        setSnapshot((current) => ({
          ...current,
          payments: removeById(current.payments, paymentId, (item) => item.paymentId),
        }));
      if (snapshot.source === "demo") {
        commit();
        setNotice("Payment removed.");
        return true;
      }
      try {
        await paymentApi.remove(paymentId);
        commit();
        setNotice("Payment removed.");
        return true;
      } catch (error) {
        setNotice(toErrorMessage(error));
        return false;
      }
    },
    [snapshot.source],
  );

  const saveMaintenance = useCallback(
    async (request: MaintenanceRequest, mode: SaveMode) => {
      const commit = (saved: MaintenanceRequest) =>
        setSnapshot((current) => ({
          ...current,
          maintenance: upsert(current.maintenance, saved, (item) => item.requestId),
        }));
      if (snapshot.source === "demo") {
        commit(request);
        setNotice("Maintenance request saved.");
        return true;
      }
      try {
        const saved =
          mode === "create"
            ? await maintenanceApi.create(request)
            : await maintenanceApi.update(request.requestId, request);
        commit({ ...request, ...saved, requestId: saved.requestId || request.requestId, status: request.status });
        setNotice("Maintenance request saved.");
        return true;
      } catch (error) {
        setNotice(toErrorMessage(error));
        return false;
      }
    },
    [snapshot.source],
  );

  const deleteMaintenance = useCallback(
    async (requestId: string) => {
      const commit = () =>
        setSnapshot((current) => ({
          ...current,
          maintenance: removeById(current.maintenance, requestId, (item) => item.requestId),
        }));
      if (snapshot.source === "demo") {
        commit();
        setNotice("Maintenance request removed.");
        return true;
      }
      try {
        await maintenanceApi.remove(requestId);
        commit();
        setNotice("Maintenance request removed.");
        return true;
      } catch (error) {
        setNotice(toErrorMessage(error));
        return false;
      }
    },
    [snapshot.source],
  );

  const value = useMemo<PortfolioContextValue>(
    () => ({
      ...snapshot,
      loading,
      refreshing,
      notice,
      dismissNotice: () => setNotice(null),
      retryConnection,
      resetDemo,
      saveProperty,
      deleteProperty,
      saveTenant,
      deleteTenant,
      saveLease,
      deleteLease,
      savePayment,
      deletePayment,
      saveMaintenance,
      deleteMaintenance,
    }),
    [
      snapshot,
      loading,
      refreshing,
      notice,
      retryConnection,
      resetDemo,
      saveProperty,
      deleteProperty,
      saveTenant,
      deleteTenant,
      saveLease,
      deleteLease,
      savePayment,
      deletePayment,
      saveMaintenance,
      deleteMaintenance,
    ],
  );

  return <PortfolioContext.Provider value={value}>{children}</PortfolioContext.Provider>;
}

export function usePortfolio() {
  const context = useContext(PortfolioContext);
  if (!context) throw new Error("usePortfolio must be used within PortfolioProvider");
  return context;
}
