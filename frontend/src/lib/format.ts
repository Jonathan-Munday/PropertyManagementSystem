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

export function todayIso(date = new Date()) {
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

export function isLeaseActive(endDate: string, today = new Date()) {
  if (!endDate) return true;
  const end = new Date(`${endDate.slice(0, 10)}T00:00:00`);
  const day = new Date(today.getFullYear(), today.getMonth(), today.getDate());
  return end >= day;
}
