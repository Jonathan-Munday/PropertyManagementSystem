export type PaymentStatus = "PAID" | "PENDING" | "OVERDUE";
export type MaintenanceStatus = "OPEN" | "IN_PROGRESS" | "RESOLVED";

/** Matches za.ac.mycput.domain.Property. Jackson usually emits `available` for isAvailable(). */
export interface Property {
  propertyId: string;
  address: string;
  description: string;
  rentalAmount: number;
  available: boolean;
}

/** Matches za.ac.mycput.domain.Tenant. */
export interface Tenant {
  tenantId: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
}

/**
 * Matches za.ac.mycput.domain.Lease.
 * startDate and endDate are kept for the agreement screen. The Java entity
 * does not store them, so a strict controller will ignore the extra fields.
 */
export interface Lease {
  leaseId: string;
  propertyId: string;
  tenantName: string;
  monthlyRent: number;
  startDate: string;
  endDate: string;
}

/**
 * Matches za.ac.mycput.domain.Payment.
 * status is a desk-side field. The Java entity stores method, amount, and date.
 */
export interface Payment {
  paymentId: string;
  leaseId: string;
  amount: number;
  paymentDate: string;
  paymentMethod: string;
  status: PaymentStatus;
}

/** Matches za.ac.mycput.domain.Maintenance. */
export interface MaintenanceRequest {
  requestId: string;
  description: string;
  status: MaintenanceStatus;
  reportedDate: string;
  cost: number;
}

/** Matches za.ac.mycput.domain.Manager. */
export interface Manager {
  managerId: string;
  name: string;
  contactNumber: string;
  employeeId: string;
}

/** Matches za.ac.mycput.domain.Agent. */
export interface Agent {
  agentId: string;
  name: string;
  email: string;
  role: string;
}

export type SaveMode = "create" | "update";
