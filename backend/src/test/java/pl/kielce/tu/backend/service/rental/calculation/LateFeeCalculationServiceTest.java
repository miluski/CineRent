package pl.kielce.tu.backend.service.rental.calculation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        LocalDateTime dueDate = LocalDateTime.now();
        LocalDateTime returnDate = LocalDateTime.now().minusDays(1);

        rental.setRentalEnd(dueDate);
        rental.setReturnDate(returnDate);

        BigDecimal lateFee = lateFeeService.calculateLateFee(rental);

        assertEquals(BigDecimal.ZERO, lateFee);
    }

    @Test
    void shouldCalculateLateFeeForOverdueReturn() {
        Rental rental = createTestRental();
        LocalDateTime dueDate = LocalDateTime.now().minusDays(5);
        LocalDateTime returnDate = LocalDateTime.now();

        rental.setRentalEnd(dueDate);
        rental.setReturnDate(returnDate);

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
