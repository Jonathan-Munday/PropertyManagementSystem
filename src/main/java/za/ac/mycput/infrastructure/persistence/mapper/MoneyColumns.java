package za.ac.mycput.infrastructure.persistence.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class MoneyColumns {

    private MoneyColumns() {
    }

    static BigDecimal fromDouble(double amount) {
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }

    static double toDouble(BigDecimal amount) {
        return amount.doubleValue();
    }
}
