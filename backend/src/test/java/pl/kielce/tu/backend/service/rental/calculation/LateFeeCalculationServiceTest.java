package pl.kielce.tu.backend.service.rental.calculation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.util.UserContextLogger;

class LateFeeCalculationServiceTest {

    private LateFeeCalculationService lateFeeService;
    private UserContextLogger userContextLogger;

    @BeforeEach
    void setUp() {
        userContextLogger = mock(UserContextLogger.class);
        lateFeeService = new LateFeeCalculationService(userContextLogger);
    }

    @Test
    void shouldReturnZeroForOnTimeReturn() {
        Rental rental = createTestRental();
        LocalDate dueDate = LocalDate.now();
        LocalDate returnDate = LocalDate.now().minusDays(1);

        rental.setRentalEnd(Date.valueOf(dueDate));
        rental.setReturnDate(Date.valueOf(returnDate));

        BigDecimal lateFee = lateFeeService.calculateLateFee(rental);

        assertEquals(BigDecimal.ZERO, lateFee);
    }

    @Test
    void shouldCalculateLateFeeForOverdueReturn() {
        Rental rental = createTestRental();
        LocalDate dueDate = LocalDate.now().minusDays(5);
        LocalDate returnDate = LocalDate.now();

        rental.setRentalEnd(Date.valueOf(dueDate));
        rental.setReturnDate(Date.valueOf(returnDate));

        BigDecimal lateFee = lateFeeService.calculateLateFee(rental);

        assertTrue(lateFee.compareTo(BigDecimal.ZERO) > 0);
    }

    private Rental createTestRental() {
        Rental rental = new Rental();

        Dvd dvd = new Dvd();
        dvd.setRentalPricePerDay(5.00f);
        rental.setDvd(dvd);

        return rental;
    }
}
