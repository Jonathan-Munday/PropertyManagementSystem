package za.ac.mycput.domain.model;

/**
 * Derived from the lease term and a calendar day. This is not stored.
 * A row with a missing start or end date is not treated as active.
 */
public enum LeaseTermStatus {
    ACTIVE,
    ENDED,
    UPCOMING,
    NEEDS_DATES
}
