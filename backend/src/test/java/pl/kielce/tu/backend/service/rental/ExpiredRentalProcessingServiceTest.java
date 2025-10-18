package pl.kielce.tu.backend.service.rental;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.model.constant.RentalStatus;
import pl.kielce.tu.backend.model.dto.ExpiredRentalProcessingDto;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.repository.RentalRepository;
import pl.kielce.tu.backend.service.rental.strategy.ReturnRequestStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpiredRentalProcessingService Tests")
class ExpiredRentalProcessingServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private ReturnRequestStrategy returnRequestStrategy;

    @Mock
    private UserContextLogger userContextLogger;

    @InjectMocks
    private ExpiredRentalProcessingService service;

    private Rental expiredRental;
    private List<Rental> expiredRentals;

    @BeforeEach
    void setUp() {
        expiredRental = createExpiredRental(1L);
        expiredRentals = Arrays.asList(
                expiredRental,
                createExpiredRental(2L),
                createExpiredRental(3L));
    }

    @Test
    @DisplayName("Should process all expired rentals successfully")
    void shouldProcessAllExpiredRentalsSuccessfully() {

        when(rentalRepository.findExpiredActiveRentals(any(LocalDateTime.class)))
                .thenReturn(expiredRentals);
        when(returnRequestStrategy.canProcess(any(Rental.class)))
                .thenReturn(true);

        ExpiredRentalProcessingDto result = service.processExpiredRentals();

        assertNotNull(result);
        assertEquals(3, result.getTotalExpiredRentals());
        assertEquals(3, result.getProcessedSuccessfully());
        assertEquals(0, result.getFailedToProcess());
        verify(returnRequestStrategy, times(3)).processReturnRequest(any(Rental.class));
    }

    @Test
    @DisplayName("Should handle no expired rentals")
    void shouldHandleNoExpiredRentals() {

        when(rentalRepository.findExpiredActiveRentals(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        ExpiredRentalProcessingDto result = service.processExpiredRentals();

        assertNotNull(result);
        assertEquals(0, result.getTotalExpiredRentals());
        assertEquals(0, result.getProcessedSuccessfully());
        assertEquals(0, result.getFailedToProcess());
        verify(returnRequestStrategy, never()).processReturnRequest(any(Rental.class));
    }

    @Test
    @DisplayName("Should handle partial processing failures")
    void shouldHandlePartialProcessingFailures() {

        when(rentalRepository.findExpiredActiveRentals(any(LocalDateTime.class)))
                .thenReturn(expiredRentals);
        when(returnRequestStrategy.canProcess(any(Rental.class)))
                .thenReturn(true, false, true);

        ExpiredRentalProcessingDto result = service.processExpiredRentals();

        assertNotNull(result);
        assertEquals(3, result.getTotalExpiredRentals());
        assertEquals(2, result.getProcessedSuccessfully());
        assertEquals(1, result.getFailedToProcess());
    }

    @Test
    @DisplayName("Should handle exception during processing")
    void shouldHandleExceptionDuringProcessing() {

        when(rentalRepository.findExpiredActiveRentals(any(LocalDateTime.class)))
                .thenReturn(expiredRentals);
        when(returnRequestStrategy.canProcess(any(Rental.class)))
                .thenReturn(true);
        doThrow(new RuntimeException("Processing error"))
                .when(returnRequestStrategy).processReturnRequest(any(Rental.class));

        ExpiredRentalProcessingDto result = service.processExpiredRentals();

        assertNotNull(result);
        assertEquals(3, result.getTotalExpiredRentals());
        assertEquals(0, result.getProcessedSuccessfully());
        assertEquals(3, result.getFailedToProcess());
    }

    private Rental createExpiredRental(Long id) {
        return Rental.builder()
                .id(id)
                .status(RentalStatus.ACTIVE)
                .rentalEnd(LocalDateTime.now().minusDays(1))
                .build();
    }
}
