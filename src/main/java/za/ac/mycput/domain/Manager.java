package za.ac.mycput.domain;

/*
  Manager.java
  Entity for the Manager
  Author: Anas Pansari (230195342)
  Date: 25th March 2026
 */

public class Manager {

    private String managerId;
    private String name;
    private String contactNumber;
    private String employeeId;

    private Manager(Builder builder) {
        this.managerId = builder.managerId;
        this.name = builder.name;
        this.contactNumber = builder.contactNumber;
        this.employeeId = builder.employeeId;
    }

    public String getManagerId() {
        return managerId;
    }

    public String getName() {
        return name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public static class Builder{

        private String managerId;
        private String name;
        private String contactNumber;
        private String employeeId;

        public Builder setManagerId(String managerId) {
            this.managerId = managerId;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setContactNumber(String contactNumber) {
            this.contactNumber = contactNumber;
            return this;
        }

        public Builder setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public Manager build() {
            return new Manager(this);
        }

        @Override
        public String toString() {
            return "Manager{" +
                    "managerId='" + managerId + '\'' +
                    ", name= ' " + name + '\'' +
                    '}';

        }
    }


}
