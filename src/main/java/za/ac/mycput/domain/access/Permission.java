package za.ac.mycput.domain.access;

/**
 * Actions an organisation role may be allowed to perform.
 * Billing is the software subscription, not the rent ledger.
 */
public enum Permission {
    VIEW_PROPERTIES,
    MANAGE_PROPERTIES,
    MANAGE_TENANTS,
    MANAGE_LEASES,
    VIEW_PAYMENTS,
    MANAGE_PAYMENTS,
    VIEW_OPERATIONS,
    MANAGE_OPERATIONS,
    VIEW_BILLING,
    MANAGE_BILLING,
    VIEW_MEMBERS,
    MANAGE_USERS,
    DELETE_ORGANISATION
}
