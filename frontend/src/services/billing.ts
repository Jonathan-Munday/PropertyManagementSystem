import { api } from "./api.ts";

export interface SubscriptionPlan {
  code: string;
  name: string;
  amount: number;
  currency: string;
  interval: string;
  maxProperties: number;
  maxUsers: number;
}

export interface SubscriptionUsage {
  properties: number;
  users: number;
  maxProperties: number | null;
  maxUsers: number | null;
}

export interface SubscriptionHistoryItem {
  planCode: string | null;
  status: string;
  provider: string;
  amount: number;
  issuedOn: string;
  detail: string;
}

export interface OrganisationSubscription {
  provider: string;
  status: string;
  detail: string;
  billingMode: string;
  demoCheckout: boolean;
  credentialsPresent: boolean;
  liveChargesEnabled: boolean;
  plan: SubscriptionPlan | null;
  usage: SubscriptionUsage;
  history: SubscriptionHistoryItem[];
}

function asRecord(value: unknown): Record<string, unknown> | null {
  if (value && typeof value === "object" && !Array.isArray(value)) return value as Record<string, unknown>;
  return null;
}

function text(value: unknown) {
  return typeof value === "string" ? value : "";
}

function number(value: unknown) {
  return typeof value === "number" && Number.isFinite(value) ? value : 0;
}

function plan(value: unknown): SubscriptionPlan | null {
  const record = asRecord(value);
  if (!record || typeof record.code !== "string") return null;
  return {
    code: record.code,
    name: text(record.name) || record.code,
    amount: number(record.amount),
    currency: text(record.currency) || "ZAR",
    interval: text(record.interval) || "MONTH",
    maxProperties: number(record.maxProperties),
    maxUsers: number(record.maxUsers),
  };
}

function readSubscription(value: unknown): OrganisationSubscription {
  const record = asRecord(value);
  if (!record || typeof record.status !== "string") {
    throw new Error("The subscription response was incomplete.");
  }
  const usage = asRecord(record.usage);
  const history = Array.isArray(record.history)
    ? record.history.map((item) => {
        const row = asRecord(item);
        return {
          planCode: row && typeof row.planCode === "string" ? row.planCode : null,
          status: row ? text(row.status) : "",
          provider: row ? text(row.provider) : "",
          amount: row ? number(row.amount) : 0,
          issuedOn: row ? text(row.issuedOn) : "",
          detail: row ? text(row.detail) : "",
        };
      })
    : [];
  return {
    provider: text(record.provider),
    status: record.status,
    detail: text(record.detail),
    billingMode: text(record.billingMode) || "NONE",
    demoCheckout: record.demoCheckout !== false,
    credentialsPresent: record.credentialsPresent === true,
    liveChargesEnabled: record.liveChargesEnabled === true,
    plan: plan(record.plan),
    usage: {
      properties: usage ? number(usage.properties) : 0,
      users: usage ? number(usage.users) : 0,
      maxProperties: usage && typeof usage.maxProperties === "number" ? usage.maxProperties : null,
      maxUsers: usage && typeof usage.maxUsers === "number" ? usage.maxUsers : null,
    },
    history,
  };
}

function readPlans(value: unknown): SubscriptionPlan[] {
  if (!Array.isArray(value)) return [];
  return value.map(plan).filter((item): item is SubscriptionPlan => item !== null);
}

export async function fetchPlans() {
  const response = await api.get("/billing/plans");
  return readPlans(response.data);
}

export async function fetchSubscription() {
  const response = await api.get("/billing/subscription");
  return readSubscription(response.data);
}

export async function changeSubscription(planCode: string, outcome: "SUCCESS" | "CANCELED" | "FAILED") {
  const response = await api.post("/billing/subscription", { planCode, outcome });
  return readSubscription(response.data);
}

export async function cancelSubscription() {
  const response = await api.post("/billing/subscription/cancel");
  return readSubscription(response.data);
}
