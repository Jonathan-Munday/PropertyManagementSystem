package za.ac.mycput.domain.valueobject;

import za.ac.mycput.domain.exception.DomainRuleException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * An amount of money in a single currency. Equality is by currency and scaled amount.
 * The portfolio currently records South African rand.
 */
public final class Money {

    public static final String ZAR = "ZAR";

    private final BigDecimal amount;
    private final String currency;

    private Money(BigDecimal amount, String currency) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    public static Money zar(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new DomainRuleException("A money amount must be a finite number");
        }
        return new Money(BigDecimal.valueOf(amount), ZAR);
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount.equals(money.amount) && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return currency + " " + amount.toPlainString();
    }
}
