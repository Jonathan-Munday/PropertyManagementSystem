/*
 * Agent.java
 * Agent domain entity representing a real estate agent.
 * Author: Zia Modarrisi (230671942)
 * Date: 28 June 2026
 */
package za.ac.mycput.domain;

import java.util.Objects;

public class Agent {
    private final String agentId;
    private final String name;
    private final String email;
    private final String role;

    private Agent(Builder builder) {
        this.agentId = builder.agentId;
        this.name = builder.name;
        this.email = builder.email;
        this.role = builder.role;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Agent)) return false;
        Agent agent = (Agent) o;
        return agentId != null && agentId.equals(agent.agentId);
    }

    @Override
    public int hashCode() {
        return agentId != null ? agentId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Agent{" +
                "agentId='" + agentId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }

    public static class Builder {
        private String agentId;
        private String name;
        private String email;
        private String role;

        public Builder setAgentId(String agentId) {
            this.agentId = agentId;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setRole(String role) {
            this.role = role;
            return this;
        }

        public Builder copy(Agent agent) {
            this.agentId = agent.agentId;
            this.name = agent.name;
            this.email = agent.email;
            this.role = agent.role;
            return this;
        }

        public Agent build() {
            return new Agent(this);
        }
    }
}
