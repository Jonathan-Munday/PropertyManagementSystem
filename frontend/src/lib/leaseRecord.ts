import type { Lease } from "../types/index.ts";

export interface LeaseFormInput {
  existing: Pick<Lease, "propertyId" | "tenantId"> | null;
  leaseId: string;
  propertyId: string;
  tenantId: string;
  tenantName: string;
  monthlyRent: number;
  startDate: string;
  endDate: string;
}

/**
 * Builds the lease the form saves. An edit keeps the original property.
 * A name-only placeholder is not sent as a tenant id.
 */
export function buildLeaseRecord(input: LeaseFormInput): Lease | null {
  if (input.tenantId.startsWith("name:")) return null;
  const tenantId = input.tenantId || input.existing?.tenantId;
  if (!tenantId) return null;
  return {
    leaseId: input.leaseId,
    propertyId: input.existing?.propertyId ?? input.propertyId,
    tenantId,
    tenantName: input.tenantName,
    monthlyRent: input.monthlyRent,
    startDate: input.startDate,
    endDate: input.endDate,
  };
}

export function toLeaseRequest(lease: Lease) {
  return {
    leaseId: lease.leaseId,
    propertyId: lease.propertyId,
    tenantId: lease.tenantId || undefined,
    tenantName: lease.tenantName,
    monthlyRent: lease.monthlyRent,
    startDate: lease.startDate || undefined,
    endDate: lease.endDate || undefined,
  };
}
