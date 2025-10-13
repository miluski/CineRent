package pl.kielce.tu.backend.service.rental.calculation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import pl.kielce.tu.backend.model.entity.Rental;

class RentalCalculationServiceTest {

    private final RentalCalculationService service = new RentalCalculationService();

    @Test
    void calculateRentalDays_sameDay_returnsOne() {
        Rental rental = mock(Rental.class);
        Date date = Date.valueOf(LocalDate.of(2025, 1, 1));
        when(rental.getRentalStart()).thenReturn(date);
        when(rental.getReturnDate()).thenReturn(date);

        long days = service.calculateRentalDays(rental);

        assertEquals(1L, days);
    }

    @Test
    void calculateRentalDays_multipleDays_inclusiveCount() {
        Rental rental = mock(Rental.class);
        Date start = Date.valueOf(LocalDate.of(2025, 1, 1));
        Date end = Date.valueOf(LocalDate.of(2025, 1, 3));
        when(rental.getRentalStart()).thenReturn(start);
        when(rental.getReturnDate()).thenReturn(end);

        long days = service.calculateRentalDays(rental);

        assertEquals(3L, days);
    }

    @Test
    void calculateBaseAmount_computesPricePerDay_timesDays_timesCount() {
        Rental rental = mock(Rental.class, Mockito.RETURNS_DEEP_STUBS);
        when(rental.getDvd().getRentalPricePerDay()).thenReturn(2.5f);
        when(rental.getCount()).thenReturn(2);

        long rentalDays = 3L;
        BigDecimal base = service.calculateBaseAmount(rental, rentalDays);

        assertEquals(BigDecimal.valueOf(15.0), base);
    }

    @Test
    void calculateTotalAmount_addsBaseAndLateFee() {
        BigDecimal base = BigDecimal.valueOf(10.00);
        BigDecimal lateFee = BigDecimal.valueOf(2.50);

        BigDecimal total = service.calculateTotalAmount(base, lateFee);

        assertEquals(BigDecimal.valueOf(12.50), total);
    }
}
