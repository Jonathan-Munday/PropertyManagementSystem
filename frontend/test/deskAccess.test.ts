import assert from "node:assert/strict";
import test from "node:test";
import { resolveApiBase, resourceUrl } from "../src/lib/apiBase.ts";
import { allowsDemoFallback } from "../src/lib/demoMode.ts";
import { formatApiError } from "../src/lib/httpError.ts";
import { canManageBilling, canManageLeases, canViewBilling } from "../src/lib/permissions.ts";

test("a v1 API base still posts a lease to /api/lease", () => {
  const base = resolveApiBase("http://localhost:8080/api/v1");
  assert.equal(resourceUrl(base, "/lease"), "http://localhost:8080/api/lease");
  assert.equal(resourceUrl(base, "/v1/me"), "http://localhost:8080/api/v1/me");
  assert.equal(resourceUrl(resolveApiBase(undefined), "/billing/subscription"), "http://localhost:8080/api/billing/subscription");
});

test("lease and billing errors keep the backend code", () => {
  const denied = formatApiError(403, {
    error: "FORBIDDEN",
    message: "Your viewer role cannot create or change leases. An owner, admin, or property manager can.",
  });
  assert.match(denied, /viewer role cannot create or change leases/);
  assert.match(denied, /FORBIDDEN/);

  const organisation = formatApiError(403, {
    error: "ORGANISATION_ACCESS",
    message: "You are not a member of that organisation",
  });
  assert.match(organisation, /ORGANISATION_ACCESS/);
  assert.match(organisation, /selected organisation/);

  assert.match(formatApiError(403, { error: "EMAIL_NOT_VERIFIED", message: "Confirm your email" }), /EMAIL_NOT_VERIFIED/);
  assert.match(formatApiError(403, "Invalid CORS request"), /403/);
});

test("lease and billing permissions follow the organisation role", () => {
  for (const role of ["OWNER", "ADMIN", "PROPERTY_MANAGER"]) {
    assert.equal(canManageLeases(role), true, role);
  }
  assert.equal(canManageLeases("ACCOUNTANT"), false);
  assert.equal(canManageLeases("VIEWER"), false);
  assert.equal(canViewBilling("OWNER"), true);
  assert.equal(canViewBilling("ADMIN"), true);
  assert.equal(canViewBilling("PROPERTY_MANAGER"), false);
  assert.equal(canManageBilling("OWNER"), true);
  assert.equal(canManageBilling("ADMIN"), false);
});

test("a production build does not fall back to the sample portfolio", () => {
  assert.equal(allowsDemoFallback(true, undefined), false);
  assert.equal(allowsDemoFallback(true, "true"), false);
  assert.equal(allowsDemoFallback(false, "false"), false);
  assert.equal(allowsDemoFallback(false, undefined), true);
});
