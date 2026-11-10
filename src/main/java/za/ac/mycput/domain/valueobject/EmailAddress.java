package za.ac.mycput.domain.valueobject;

import za.ac.mycput.domain.exception.DomainRuleException;

import java.util.Objects;
import java.util.regex.Pattern;

public final class EmailAddress {

    private static final Pattern PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final String value;

    public EmailAddress(String value) {
        if (value == null || !PATTERN.matcher(value.trim()).matches()) {
            throw new DomainRuleException("Email address is invalid");
        }
        this.value = value.trim();
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailAddress other)) return false;
        return value.equalsIgnoreCase(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value.toLowerCase());
    }

    @Override
    public String toString() {
        return value;
    }
}
