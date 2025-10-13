package pl.kielce.tu.backend.service.rental.calculation;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.constant.CalculationConstants;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class LateFeeCalculationService {

    private final UserContextLogger userContextLogger;

    public BigDecimal calculateLateFee(Rental rental) {
        logStart(rental);

        Date returnDate = rental.getReturnDate();
        Date dueDate = rental.getRentalEnd();
        
        if (isReturnOnTime(returnDate, dueDate)) {
            logOnTimeCompletion(rental);
            return BigDecimal.ZERO;
        }

        long overdueDays = calculateOverdueDays(returnDate, dueDate);
        BigDecimal lateFee = computeLateFeeForRental(rental, overdueDays);

        logCompletedWithFee(rental, overdueDays, lateFee);
        return lateFee;
    }

    private void logStart(Rental rental) {
        userContextLogger.logUserOperation("LATE_FEE_CALCULATION_STARTED",
                "Rental ID: " + rental.getId());
    }

    private void logOnTimeCompletion(Rental rental) {
        userContextLogger.logUserOperation("LATE_FEE_CALCULATION_COMPLETED",
                "Rental ID: " + rental.getId() + ", Fee: 0.00 (on time)");
    }

    private BigDecimal computeLateFeeForRental(Rental rental, long overdueDays) {
        BigDecimal basePricePerDay = getBasisPricePerDay(rental);
        return calculateTotalLateFee(basePricePerDay, overdueDays);
    }

    private void logCompletedWithFee(Rental rental, long overdueDays, BigDecimal lateFee) {
        userContextLogger.logUserOperation("LATE_FEE_CALCULATION_COMPLETED",
                String.format("Rental ID: %d, Overdue days: %d, Fee: %s",
                        rental.getId(), overdueDays, lateFee));
    }

    private boolean isReturnOnTime(Date returnDate, Date dueDate) {
        return returnDate.compareTo(dueDate) <= 0;
    }

    private long calculateOverdueDays(Date returnDate, Date dueDate) {
        return ChronoUnit.DAYS.between(dueDate.toLocalDate(), returnDate.toLocalDate());
    }

    private BigDecimal getBasisPricePerDay(Rental rental) {
        return BigDecimal.valueOf(rental.getDvd().getRentalPricePerDay());
    }

    private BigDecimal calculateTotalLateFee(BigDecimal basePricePerDay, long overdueDays) {
        BigDecimal overdueAmount = BigDecimal.valueOf(overdueDays);
        BigDecimal multiplier = CalculationConstants.LATE_FEE_MULTIPLIER.getValue();
        return basePricePerDay.multiply(multiplier).multiply(overdueAmount);
    }

}
