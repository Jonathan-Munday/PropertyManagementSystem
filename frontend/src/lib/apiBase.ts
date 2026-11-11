/**
 * Portfolio routes are under /api. Account routes already include /v1.
 * A configured base of http://localhost:8080/api/v1 is normalised so /lease
 * is posted to /api/lease, which is the lease permission route.
 */
export function resolveApiBase(configured: string | undefined) {
  const raw = (configured?.trim() || "http://localhost:8080/api").replace(/\/+$/, "");
  return raw.endsWith("/api/v1") ? raw.slice(0, -3) : raw;
}

export function resourceUrl(base: string, path: string) {
  const resource = path.startsWith("/") ? path : `/${path}`;
  return `${base.replace(/\/+$/, "")}${resource}`;
}
