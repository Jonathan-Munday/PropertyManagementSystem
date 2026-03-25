package za.ac.mycput.domain;

public class Lease {
    private String leaseId;
    private String propertyId;
    private String tenantName;
    private double monthlyRent;

    // Standard constructor
    public Lease(String leaseId, String propertyId, String tenantName, double monthlyRent) {
        this.leaseId = leaseId;
        this.propertyId = propertyId;
        this.tenantName = tenantName;
        this.monthlyRent = monthlyRent;
    }
    // Getters
    public String getLeaseId() { return leaseId; }
    public String getPropertyId() { return propertyId; }
    public String getTenantName() { return tenantName; }
    public double getMonthlyRent() { return monthlyRent; }

    public void setMonthlyRent(double v) {
        this.monthlyRent= v;
    }
}