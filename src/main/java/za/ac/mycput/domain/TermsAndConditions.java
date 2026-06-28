/*
 * TermsAndConditions.java
 * Supporting entity for lease terms and conditions.
 * Author: Malik Muhammed (230388175)
 * Date: 28 June 2026
 */
package za.ac.mycput.domain;

import java.util.Objects;

public class TermsAndConditions {
    private final String termsId;
    private final String leaseId;
    private final String description;
    private final boolean accepted;

    private TermsAndConditions(Builder builder) {
        this.termsId = builder.termsId;
        this.leaseId = builder.leaseId;
        this.description = builder.description;
        this.accepted = builder.accepted;
    }

    public String getTermsId() {
        return termsId;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAccepted() {
        return accepted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TermsAndConditions)) return false;
        TermsAndConditions that = (TermsAndConditions) o;
        return termsId != null && termsId.equals(that.termsId);
    }

    @Override
    public int hashCode() {
        return termsId != null ? termsId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "TermsAndConditions{" +
                "termsId='" + termsId + '\'' +
                ", leaseId='" + leaseId + '\'' +
                ", description='" + description + '\'' +
                ", accepted=" + accepted +
                '}';
    }

    public static class Builder {
        private String termsId;
        private String leaseId;
        private String description;
        private boolean accepted;

        public Builder setTermsId(String termsId) {
            this.termsId = termsId;
            return this;
        }

        public Builder setLeaseId(String leaseId) {
            this.leaseId = leaseId;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setAccepted(boolean accepted) {
            this.accepted = accepted;
            return this;
        }

        public Builder copy(TermsAndConditions t) {
            this.termsId = t.termsId;
            this.leaseId = t.leaseId;
            this.description = t.description;
            this.accepted = t.accepted;
            return this;
        }

        public TermsAndConditions build() {
            return new TermsAndConditions(this);
        }
    }
}

