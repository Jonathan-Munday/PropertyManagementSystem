export type OrganisationRole = "OWNER" | "ADMIN" | "PROPERTY_MANAGER" | "ACCOUNTANT" | "VIEWER";

const labels: Record<OrganisationRole, string> = {
  OWNER: "Owner",
  ADMIN: "Admin",
  PROPERTY_MANAGER: "Property manager",
  ACCOUNTANT: "Accountant",
  VIEWER: "Viewer",
};

export function roleLabel(role: string | undefined) {
  if (!role) return "";
  return labels[role as OrganisationRole] ?? role;
}

export function grantableRoles(role: string | undefined): OrganisationRole[] {
  if (role === "OWNER") return ["ADMIN", "PROPERTY_MANAGER", "ACCOUNTANT", "VIEWER", "OWNER"];
  if (role === "ADMIN") return ["ADMIN", "PROPERTY_MANAGER", "ACCOUNTANT", "VIEWER"];
  return [];
}

export function canManageLeases(role: string | undefined) {
  return role === "OWNER" || role === "ADMIN" || role === "PROPERTY_MANAGER";
}

export function canViewBilling(role: string | undefined) {
  return role === "OWNER" || role === "ADMIN";
}

export function canManageBilling(role: string | undefined) {
  return role === "OWNER";
}
