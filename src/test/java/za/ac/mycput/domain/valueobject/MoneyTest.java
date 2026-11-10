package za.ac.mycput.domain.valueobject;

import org.junit.jupiter.api.Test;
import za.ac.mycput.domain.exception.DomainRuleException;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void scalesRandToCents() {
        Money money = Money.zar(10);
        assertEquals("10.00", money.amount().toPlainString());
        assertEquals(Money.ZAR, money.currency());
        assertEquals(Money.zar(10.0), money);
    }

    @Test
    void rejectsNonFiniteAmounts() {
        assertThrows(DomainRuleException.class, () -> Money.zar(Double.NaN));
        assertThrows(DomainRuleException.class, () -> Money.zar(Double.POSITIVE_INFINITY));
    }

    @Test
    void distinguishesSign() {
        assertTrue(Money.zar(1).isPositive());
        assertTrue(Money.zar(0).isZero());
        assertTrue(Money.zar(-0.01).isNegative());
    }
}
