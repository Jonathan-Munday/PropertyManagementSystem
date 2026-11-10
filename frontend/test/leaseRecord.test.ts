import assert from "node:assert/strict";
import test from "node:test";
import { businessToday, isLeaseActive, leaseTermStatus, todayIso } from "../src/lib/format.ts";
import { buildLeaseRecord, toLeaseRequest } from "../src/lib/leaseRecord.ts";

test("lease request payload includes the tenant, property, term, and rent", () => {
  const lease = buildLeaseRecord({
    existing: null,
    leaseId: "11111111-1111-4111-8111-111111111111",
    propertyId: "22222222-2222-4222-8222-222222222222",
    tenantId: "33333333-3333-4333-8333-333333333333",
    tenantName: "Ada Lovelace",
    monthlyRent: 9500,
    startDate: "2026-11-01",
    endDate: "2027-10-31",
  });

  assert.ok(lease);
  const body = JSON.parse(JSON.stringify(toLeaseRequest(lease))) as Record<string, unknown>;
  assert.equal(body.tenantId, "33333333-3333-4333-8333-333333333333");
  assert.equal(body.propertyId, "22222222-2222-4222-8222-222222222222");
  assert.equal(body.startDate, "2026-11-01");
  assert.equal(body.endDate, "2027-10-31");
  assert.equal(body.monthlyRent, 9500);
});

test("an edit keeps the original property and tenant when those fields are unchanged", () => {
  const lease = buildLeaseRecord({
    existing: { propertyId: "property-original", tenantId: "tenant-original" },
    leaseId: "lease-1",
    propertyId: "property-changed",
    tenantId: "",
    tenantName: "Ada Lovelace",
    monthlyRent: 1000,
    startDate: "2026-01-01",
    endDate: "2026-12-31",
  });

  assert.equal(lease?.propertyId, "property-original");
  assert.equal(lease?.tenantId, "tenant-original");
  const body = JSON.parse(JSON.stringify(toLeaseRequest(lease!))) as Record<string, unknown>;
  assert.equal(body.tenantId, "tenant-original");
  assert.equal(body.propertyId, "property-original");
});

test("a name placeholder is not sent as a tenant id", () => {
  const lease = buildLeaseRecord({
    existing: { propertyId: "property-original", tenantId: "tenant-original" },
    leaseId: "lease-1",
    propertyId: "property-original",
    tenantId: "name:Ada Lovelace",
    tenantName: "Ada Lovelace",
    monthlyRent: 1000,
    startDate: "2026-01-01",
    endDate: "2026-12-31",
  });

  assert.equal(lease, null);
});

test("lease status follows the saved term and does not treat missing dates as active", () => {
  const today = new Date(2026, 10, 9);
  assert.equal(leaseTermStatus({ startDate: "2026-01-01", endDate: "2026-12-31" }, today), "Active");
  assert.equal(leaseTermStatus({ startDate: "2026-12-01", endDate: "2027-12-01" }, today), "Upcoming");
  assert.equal(leaseTermStatus({ startDate: "2020-01-01", endDate: "2020-12-31" }, today), "Ended");
  assert.equal(leaseTermStatus({ startDate: "", endDate: "2099-01-01" }, today), "Needs dates");
  assert.equal(leaseTermStatus({}, today), "Needs dates");
  assert.equal(isLeaseActive({}, today), false);
  assert.equal(isLeaseActive({ startDate: "2026-01-01", endDate: "2026-12-31" }, today), true);
});

test("lease today follows Johannesburg, not UTC", () => {
  const justBeforeMidnightInJohannesburg = new Date("2026-11-09T22:30:00Z");
  assert.equal(todayIso(businessToday(justBeforeMidnightInJohannesburg)), "2026-11-10");
  assert.equal(
    leaseTermStatus(
      { startDate: "2026-11-09", endDate: "2026-11-09" },
      businessToday(justBeforeMidnightInJohannesburg),
    ),
    "Ended",
  );
  assert.equal(
    leaseTermStatus(
      { startDate: "2026-11-10", endDate: "2026-12-01" },
      businessToday(justBeforeMidnightInJohannesburg),
    ),
    "Active",
  );
});
