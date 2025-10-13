package pl.kielce.tu.backend.model.constant;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CalculationConstants {
    LATE_FEE_MULTIPLIER(BigDecimal.valueOf(10)),
    RENTAL_PERIOD_DAYS(BigDecimal.valueOf(7));

    private final BigDecimal value;
}
