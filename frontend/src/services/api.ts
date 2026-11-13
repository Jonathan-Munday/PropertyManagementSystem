import axios from "axios";
import { resolveApiBase } from "../lib/apiBase.ts";
import { formatApiError } from "../lib/httpError.ts";
import { toLeaseRequest } from "../lib/leaseRecord.ts";
import { supabase } from "../lib/supabase.ts";
import type {
  Agent,
  Lease,
  MaintenanceRequest,
  MaintenanceStatus,
  Manager,
  Payment,
  PaymentStatus,
  Property,
  Tenant,
} from "../types/index.ts";

/**
 * Portfolio routes live under /api. Account routes are /api/v1 and already
 * include the /v1 prefix. A base of http://localhost:8080/api/v1 is accepted
 * and normalised so a lease is posted to /api/lease rather than /api/v1/lease.
 */
const configuredApiBase =
  import.meta.env.VITE_PORTICO_API_BASE_URL ||
  import.meta.env.VITE_API_BASE_URL;

export const API_BASE_URL = resolveApiBase(configuredApiBase);

export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 180000,
  headers: {
    Accept: "application/json",
    "Content-Type": "application/json",
  },
});

type AuthBinding = {
  token: () => Promise<string | null>;
  organisationId: () => string | null;
};

let authBinding: AuthBinding = {
  token: async () => null,
  organisationId: () => null,
};

export function bindApiAuth(binding: AuthBinding) {
  authBinding = binding;
}

api.interceptors.request.use(async (config) => {
  const token = await authBinding.token();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  const organisationId = authBinding.organisationId();
  if (organisationId) {
    config.headers["X-Organisation-Id"] = organisationId;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const config = error?.config as { _retry?: boolean; headers?: { Authorization?: string } } | undefined;
    if (!axios.isAxiosError(error) || error.response?.status !== 401 || !config || config._retry || !supabase) {
      return Promise.reject(error);
    }
    config._retry = true;
    const { data, error: refreshError } = await supabase.auth.refreshSession();
    if (refreshError || !data.session) {
      return Promise.reject(error);
    }
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${data.session.access_token}`;
    return api.request(config);
  },
);

export function toErrorMessage(error: unknown) {
  if (axios.isAxiosError(error)) {
    if (error.code === "ECONNABORTED") return "The property API took too long to respond.";
    if (!error.response) return "The property service is unreachable.";
    return formatApiError(error.response.status, error.response.data);
  }
  return "The change could not be saved.";
}

function asRecord(value: unknown): Record<string, unknown> | null {
  if (value && typeof value === "object" && !Array.isArray(value)) {
    return value as Record<string, unknown>;
  }
  return null;
}

function unwrapList(payload: unknown): unknown[] {
  if (Array.isArray(payload)) return payload;
  const record = asRecord(payload);
  if (!record) return [];
  for (const key of [
    "data",
    "content",
    "items",
    "results",
    "properties",
    "tenants",
    "leases",
    "payments",
    "maintenance",
    "managers",
    "agents",
  ]) {
    if (Array.isArray(record[key])) return record[key];
  }
  return [];
}

function readString(record: Record<string, unknown>, ...keys: string[]) {
  for (const key of keys) {
    const value = record[key];
    if (typeof value === "string" && value.trim()) return value.trim();
    if (typeof value === "number" && Number.isFinite(value)) return String(value);
  }
  return "";
}

function readNumber(record: Record<string, unknown>, ...keys: string[]) {
  for (const key of keys) {
    const value = record[key];
    if (typeof value === "number" && Number.isFinite(value)) return value;
    if (typeof value === "string" && value.trim() && Number.isFinite(Number(value))) {
      return Number(value);
    }
  }
  return 0;
}

function readDate(value: unknown) {
  if (typeof value === "string") return value.slice(0, 10);
  if (Array.isArray(value) && value.length >= 3) {
    const [year, month, day] = value;
    if (typeof year === "number" && typeof month === "number" && typeof day === "number") {
      return `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
    }
  }
  return "";
}

function readAddress(record: Record<string, unknown>) {
  const direct = readString(record, "address");
  if (direct) return direct;
  const nested = asRecord(record.address);
  if (!nested) return "";
  return [nested.streetAddress, nested.city, nested.province, nested.postalCode]
    .filter((part): part is string => typeof part === "string" && part.trim().length > 0)
    .join(", ");
}

function readAvailable(record: Record<string, unknown>) {
  if (typeof record.available === "boolean") return record.available;
  if (typeof record.isAvailable === "boolean") return record.isAvailable;
  const text = readString(record, "status").toLowerCase();
  if (["available", "vacant", "true"].includes(text)) return true;
  if (["occupied", "leased", "unavailable", "false"].includes(text)) return false;
  return true;
}

function readPaymentStatus(record: Record<string, unknown>, paymentDate: string): PaymentStatus {
  const raw = readString(record, "status", "paymentStatus").toUpperCase();
  if (raw === "PAID" || raw === "PENDING" || raw === "OVERDUE") return raw;
  if (!paymentDate) return "PENDING";
  const date = new Date(`${paymentDate}T00:00:00`);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return date > today ? "PENDING" : "PAID";
}

function readMaintenanceStatus(value: string): MaintenanceStatus {
  const key = value.trim().toUpperCase().replace(/[\s-]+/g, "_");
  if (key === "IN_PROGRESS" || key === "INPROGRESS") return "IN_PROGRESS";
  if (key === "RESOLVED" || key === "CLOSED" || key === "DONE") return "RESOLVED";
  return "OPEN";
}

function normalizeProperty(record: Record<string, unknown>): Property | null {
  const propertyId = readString(record, "propertyId", "id");
  const address = readAddress(record);
  if (!propertyId || !address) return null;
  return {
    propertyId,
    address,
    description: readString(record, "description"),
    rentalAmount: readNumber(record, "rentalAmount", "price", "monthlyRent"),
    available: readAvailable(record),
  };
}

function normalizeTenant(record: Record<string, unknown>): Tenant | null {
  const tenantId = readString(record, "tenantId", "id");
  const firstName = readString(record, "firstName");
  if (!tenantId || !firstName) return null;
  return {
    tenantId,
    firstName,
    lastName: readString(record, "lastName"),
    email: readString(record, "email"),
    phoneNumber: readString(record, "phoneNumber", "phone", "contactNumber"),
  };
}

function normalizeLease(record: Record<string, unknown>): Lease | null {
  const leaseId = readString(record, "leaseId", "id");
  if (!leaseId) return null;
  return {
    leaseId,
    propertyId: readString(record, "propertyId"),
    tenantId: readString(record, "tenantId") || undefined,
    tenantName: readString(record, "tenantName", "tenant"),
    monthlyRent: readNumber(record, "monthlyRent", "rentalAmount", "amount"),
    startDate: readDate(record.startDate),
    endDate: readDate(record.endDate),
  };
}

function normalizePayment(record: Record<string, unknown>): Payment | null {
  const paymentId = readString(record, "paymentId", "id");
  if (!paymentId) return null;
  const paymentDate = readDate(record.paymentDate);
  return {
    paymentId,
    leaseId: readString(record, "leaseId"),
    amount: readNumber(record, "amount"),
    paymentDate,
    paymentMethod: readString(record, "paymentMethod", "method") || "EFT",
    status: readPaymentStatus(record, paymentDate),
  };
}

function normalizeMaintenance(record: Record<string, unknown>): MaintenanceRequest | null {
  const requestId = readString(record, "requestId", "maintenanceId", "id");
  const description = readString(record, "description");
  if (!requestId || !description) return null;
  return {
    requestId,
    description,
    status: readMaintenanceStatus(readString(record, "status")),
    reportedDate: readDate(record.reportedDate),
    cost: readNumber(record, "cost"),
  };
}

function normalizeManager(record: Record<string, unknown>): Manager | null {
  const managerId = readString(record, "managerId", "id");
  const name = readString(record, "name");
  if (!managerId || !name) return null;
  return {
    managerId,
    name,
    contactNumber: readString(record, "contactNumber", "phoneNumber"),
    employeeId: readString(record, "employeeId"),
  };
}

function normalizeAgent(record: Record<string, unknown>): Agent | null {
  const agentId = readString(record, "agentId", "id");
  const name = readString(record, "name");
  if (!agentId || !name) return null;
  return {
    agentId,
    name,
    email: readString(record, "email"),
    role: readString(record, "role"),
  };
}

function serializeProperty(property: Property) {
  return {
    propertyId: property.propertyId,
    address: property.address,
    description: property.description,
    rentalAmount: property.rentalAmount,
    available: property.available,
    isAvailable: property.available,
  };
}

function serializeTenant(tenant: Tenant) {
  return { ...tenant };
}

function serializeLease(lease: Lease) {
  return toLeaseRequest(lease);
}

function serializePayment(payment: Payment) {
  return {
    paymentId: payment.paymentId,
    leaseId: payment.leaseId,
    amount: payment.amount,
    paymentDate: payment.paymentDate,
    paymentMethod: payment.paymentMethod,
    status: payment.status,
  };
}

function serializeMaintenance(request: MaintenanceRequest) {
  return { ...request };
}

function serializeManager(manager: Manager) {
  return { ...manager };
}

function serializeAgent(agent: Agent) {
  return { ...agent };
}

function createResource<T>(
  path: string,
  normalize: (record: Record<string, unknown>) => T | null,
  serialize: (entity: T) => unknown,
) {
  return {
    async list() {
      const response = await api.get(path);
      return unwrapList(response.data)
        .map((item) => {
          const record = asRecord(item);
          return record ? normalize(record) : null;
        })
        .filter((item): item is T => item !== null);
    },
    async read(id: string) {
      const response = await api.get(`${path}/${encodeURIComponent(id)}`);
      const record = asRecord(response.data);
      const entity = record ? normalize(record) : null;
      if (!entity) throw new Error(`Empty response from ${path}/${id}`);
      return entity;
    },
    async create(entity: T) {
      const response = await api.post(path, serialize(entity));
      const record = asRecord(response.data);
      return record ? (normalize(record) ?? entity) : entity;
    },
    async update(id: string, entity: T) {
      const response = await api.put(`${path}/${encodeURIComponent(id)}`, serialize(entity));
      const record = asRecord(response.data);
      return record ? (normalize(record) ?? entity) : entity;
    },
    async remove(id: string) {
      await api.delete(`${path}/${encodeURIComponent(id)}`);
    },
  };
}

export const propertyApi = createResource("/property", normalizeProperty, serializeProperty);
export const tenantApi = createResource("/tenant", normalizeTenant, serializeTenant);
export const leaseApi = createResource("/lease", normalizeLease, serializeLease);
export const paymentApi = createResource("/payment", normalizePayment, serializePayment);
export const maintenanceApi = createResource("/maintenance", normalizeMaintenance, serializeMaintenance);
export const managerApi = createResource("/manager", normalizeManager, serializeManager);
export const agentApi = createResource("/agent", normalizeAgent, serializeAgent);
