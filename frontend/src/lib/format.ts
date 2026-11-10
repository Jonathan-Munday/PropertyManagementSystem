export function formatZar(amount: number) {
  return new Intl.NumberFormat("en-ZA", {
    style: "currency",
    currency: "ZAR",
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  }).format(amount);
}

export function formatDate(iso?: string) {
  if (!iso) return "—";
  const date = new Date(`${iso.slice(0, 10)}T00:00:00`);
  if (Number.isNaN(date.getTime())) return iso;
  return new Intl.DateTimeFormat("en-ZA", {
    day: "numeric",
    month: "short",
    year: "numeric",
  }).format(date);
}

export function formatPercent(value: number) {
  const percent = value * 100;
  const digits = Number.isInteger(percent) ? 0 : 1;
  return new Intl.NumberFormat("en-ZA", {
    style: "percent",
    minimumFractionDigits: digits,
    maximumFractionDigits: digits,
  }).format(value);
}

export function formatLongDate(date = new Date()) {
  return new Intl.DateTimeFormat("en-ZA", {
    weekday: "long",
    day: "numeric",
    month: "long",
  }).format(date);
}

export function formatMonth(date = new Date()) {
  return new Intl.DateTimeFormat("en-ZA", { month: "long" }).format(date);
}

export function fullName(person: { firstName: string; lastName: string }) {
  return `${person.firstName} ${person.lastName}`.trim();
}

/** Lease dates roll over at midnight in South Africa, matching the API clock. */
export const LEASE_TIME_ZONE = "Africa/Johannesburg";

export function businessToday(now = new Date()) {
  const parts = new Intl.DateTimeFormat("en-US", {
    timeZone: LEASE_TIME_ZONE,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).formatToParts(now);
  const value = (type: Intl.DateTimeFormatPartTypes) => Number(parts.find((part) => part.type === type)?.value);
  return new Date(value("year"), value("month") - 1, value("day"));
}

export function todayIso(date = businessToday()) {
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${date.getFullYear()}-${month}-${day}`;
}

export function plusYears(iso: string, years: number) {
  const date = new Date(`${iso}T00:00:00`);
  date.setFullYear(date.getFullYear() + years);
  return todayIso(date);
}

export function monthKey(date = new Date()) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
}

export function parseMoney(value: string) {
  const normalized = value.replace(/[Rr\s,]/g, "");
  if (!/^\d+(\.\d{1,2})?$/.test(normalized)) return Number.NaN;
  return Number(normalized);
}

export type LeaseTermStatus = "Active" | "Ended" | "Upcoming" | "Needs dates";

function parseIsoDay(iso?: string) {
  if (!iso) return null;
  const match = /^(\d{4})-(\d{2})-(\d{2})/.exec(iso);
  if (!match) return null;
  const date = new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3]));
  return Number.isNaN(date.getTime()) ? null : date;
}

/** Active only while today sits inside a complete term. Missing dates are not active. */
export function leaseTermStatus(
  lease: { startDate?: string; endDate?: string },
  today = businessToday(),
): LeaseTermStatus {
  const start = parseIsoDay(lease.startDate);
  const end = parseIsoDay(lease.endDate);
  if (!start || !end) return "Needs dates";
  const day = new Date(today.getFullYear(), today.getMonth(), today.getDate());
  if (end < day) return "Ended";
  if (start > day) return "Upcoming";
  return "Active";
}

export function isLeaseActive(lease: { startDate?: string; endDate?: string }, today = businessToday()) {
  return leaseTermStatus(lease, today) === "Active";
}

/** A legacy lease with no dates still reserves the property until the term is known to have ended. */
export function leaseOccupies(lease: { startDate?: string; endDate?: string }, today = businessToday()) {
  return leaseTermStatus(lease, today) !== "Ended";
}
