package pl.kielce.tu.backend.service.rental.calculation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import pl.kielce.tu.backend.model.entity.Rental;

@Component
public class RentalCalculationService {

    public long calculateRentalDays(Rental rental) {
        LocalDateTime startDate = rental.getRentalStart();
        LocalDateTime endDate = rental.getReturnDate();
        long hours = ChronoUnit.HOURS.between(startDate, endDate);
        return (long) Math.ceil(hours / 24.0);
    }

    public BigDecimal calculateBaseAmount(Rental rental, long rentalDays) {
        BigDecimal pricePerDay = getBasisPricePerDay(rental);
        int copyCount = rental.getCount();

        return calculateTotalBaseAmount(pricePerDay, rentalDays, copyCount);
    }

    public BigDecimal calculateTotalAmount(BigDecimal baseAmount, BigDecimal lateFee) {
        return baseAmount.add(lateFee);
    }

    private BigDecimal getBasisPricePerDay(Rental rental) {
        return BigDecimal.valueOf(rental.getDvd().getRentalPricePerDay());
    }

    private BigDecimal calculateTotalBaseAmount(BigDecimal pricePerDay, long days, int count) {
        return pricePerDay.multiply(BigDecimal.valueOf(days)).multiply(BigDecimal.valueOf(count));
    }
}
