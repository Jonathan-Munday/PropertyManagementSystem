package za.ac.mycput.domain;
/*
* Maintenance.java
* Maintenance for the tenant
* Olwethu Mtwazi (230036937)
* 21/3/2026
 */

import java.time.LocalDate;

public class Maintenance {
    private String requestId;
    private String description;
    private String status;
    private LocalDate reportedDate;
    private double cost;

    private Maintenance(){

    }

    private Maintenance(Builder builder){
        this.requestId = builder.requestId;
        this.description = builder.description;
        this.status = builder.status;
        this.reportedDate = builder.reportedDate;
        this.cost = builder.cost;

    }

    public String getRequestId() {
        return requestId;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getReportedDate() {
        return reportedDate;
    }

    public double getCost() {
        return cost;
    }
    public static class Builder {
        private String requestId;
        private String description;
        private String status;
        private LocalDate reportedDate;
        private double cost;

        public Builder setRequestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder setReportedDate(LocalDate reportedDate) {
            this.reportedDate = reportedDate;
            return this;
        }

        public Builder setCost(double cost) {
            this.cost = cost;
            return this;
        }

        public Builder copy(Maintenance maintenance) {
            this.requestId = maintenance.requestId;
            this.description = maintenance.description;
            this.status = maintenance.status;
            this.reportedDate = maintenance.reportedDate;
            this.cost = maintenance.cost;
            return this;
        }

        public Maintenance build() {
            return new Maintenance(this);
        }
    }

    @Override
    public String toString() {
        return "Maintenance{" +
                "requestId='" + requestId + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", reportedDate=" + reportedDate +
                ", cost=" + cost +
                '}';
    }
}

