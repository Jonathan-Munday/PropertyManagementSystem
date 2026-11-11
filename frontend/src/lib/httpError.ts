function asRecord(value: unknown): Record<string, unknown> | null {
  if (value && typeof value === "object" && !Array.isArray(value)) {
    return value as Record<string, unknown>;
  }
  return null;
}

function text(value: unknown) {
  return typeof value === "string" && value.trim() ? value.trim() : "";
}

/**
 * Turns an API error body into a sentence the desk can show.
 * The backend code stays visible so a 403 can be told apart from a role refusal,
 * an organisation mismatch, and an unverified email.
 */
export function formatApiError(status: number, data: unknown) {
  const record = asRecord(data);
  const code = record ? text(record.error) : "";
  const message = record ? text(record.message) : typeof data === "string" ? data.trim() : "";

  if (code === "EMAIL_NOT_VERIFIED") {
    return "Confirm your email before using Portico. (EMAIL_NOT_VERIFIED)";
  }
  if (code === "ORGANISATION_ACCESS") {
    const detail = message || "You are not an active member of the selected organisation.";
    return `${detail} Check the selected organisation. (ORGANISATION_ACCESS)`;
  }
  if (code === "FORBIDDEN") {
    const detail = message || "Your role does not allow this action.";
    return `${detail} (FORBIDDEN)`;
  }
  if (message && code) return `${message} (${code})`;
  if (message) return status ? `${message} (${status})` : message;
  if (status === 403) {
    return "The API refused this action (403). Your role may not allow it, or the selected organisation may not match an active membership.";
  }
  return `The property API returned status ${status}.`;
}
