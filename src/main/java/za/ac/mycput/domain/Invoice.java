package za.ac.mycput.domain;

/*
  Invoice.java
  Entity for the Invoice
  Author: Anas Pansari (230195342)
  Date: 28th June 2026
*/

public class Invoice {

    private String invoiceId;
    private String invoiceDate;
    private String amount;
    private String managerId;

    private Invoice(Builder builder) {
        this.invoiceId = builder.invoiceId;
        this.invoiceDate = builder.invoiceDate;
        this.amount = builder.amount;
        this.managerId = builder.managerId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public String getAmount() {
        return amount;
    }

    public String getManagerId() {
        return managerId;
    }

    public static class Builder {

        private String invoiceId;
        private String invoiceDate;
        private String amount;
        private String managerId;

        public Builder setInvoiceId(String invoiceId) {
            this.invoiceId = invoiceId;
            return this;
        }

        public Builder setInvoiceDate(String invoiceDate) {
            this.invoiceDate = invoiceDate;
            return this;
        }

        public Builder setAmount(String amount) {
            this.amount = amount;
            return this;
        }

        public Builder setManagerId(String managerId) {
            this.managerId = managerId;
            return this;
        }

        public Invoice build() {
            return new Invoice(this);
        }

        @Override
        public String toString() {
            return "Invoice{" +
                    "invoiceId='" + invoiceId + '\'' +
                    ", amount='" + amount + '\'' +
                    '}';
        }
    }
}

