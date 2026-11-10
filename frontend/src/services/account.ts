import { api } from "./api.ts";

export interface OrganisationMembership {
  organisationId: string;
  organisationName: string;
  role: string;
  status: string;
}

export interface Account {
  userId: string;
  email: string | null;
  displayName: string | null;
  emailVerified: boolean;
  profileComplete: boolean;
  organisations: OrganisationMembership[];
}

export interface Invitation {
  invitationId: string;
  email: string;
  role: string;
  status: string;
  expiresAt: string;
  token: string;
}

function asRecord(value: unknown): Record<string, unknown> | null {
  if (value && typeof value === "object" && !Array.isArray(value)) return value as Record<string, unknown>;
  return null;
}

function text(value: unknown) {
  return typeof value === "string" ? value : null;
}

function membership(value: unknown): OrganisationMembership | null {
  const record = asRecord(value);
  if (!record || typeof record.organisationId !== "string") return null;
  return {
    organisationId: record.organisationId,
    organisationName: text(record.organisationName) ?? "Organisation",
    role: text(record.role) ?? "VIEWER",
    status: text(record.status) ?? "ACTIVE",
  };
}

export function readAccount(value: unknown): Account {
  const record = asRecord(value);
  if (!record || typeof record.userId !== "string") {
    throw new Error("The account response was incomplete.");
  }
  const organisations = Array.isArray(record.organisations)
    ? record.organisations.map(membership).filter((item): item is OrganisationMembership => item !== null)
    : [];
  return {
    userId: record.userId,
    email: text(record.email),
    displayName: text(record.displayName),
    emailVerified: record.emailVerified === true,
    profileComplete: record.profileComplete === true,
    organisations,
  };
}

export async function fetchAccount() {
  const response = await api.get("/v1/me");
  return readAccount(response.data);
}

export async function updateProfile(displayName: string) {
  const response = await api.put("/v1/me", { displayName });
  return readAccount(response.data);
}

export async function createOrganisation(name: string, displayName: string) {
  const response = await api.post("/v1/organisations", { name, displayName });
  const record = asRecord(response.data);
  return membership(record);
}

export async function acceptInvitation(token: string) {
  const response = await api.post("/v1/invitations/accept", { token });
  return membership(asRecord(response.data));
}

export async function createInvitation(organisationId: string, email: string, role: string) {
  const response = await api.post(`/v1/organisations/${organisationId}/invitations`, { email, role });
  const record = asRecord(response.data);
  if (!record || typeof record.token !== "string") {
    throw new Error("The invitation response was incomplete.");
  }
  return {
    invitationId: text(record.invitationId) ?? "",
    email: text(record.email) ?? email,
    role: text(record.role) ?? role,
    status: text(record.status) ?? "PENDING",
    expiresAt: text(record.expiresAt) ?? "",
    token: record.token,
  } satisfies Invitation;
}
