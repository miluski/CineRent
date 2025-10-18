package pl.kielce.tu.backend.service.rental.scheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.model.dto.ExpiredRentalProcessingDto;
import pl.kielce.tu.backend.service.rental.ExpiredRentalProcessingService;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
@DisplayName("RentalScheduler Tests")
class RentalSchedulerTest {

    @Mock
    private ExpiredRentalProcessingService expiredRentalProcessingService;

    @Mock
    private UserContextLogger userContextLogger;

    @InjectMocks
    private RentalScheduler scheduler;

    private ExpiredRentalProcessingDto processingResult;

    @BeforeEach
    void setUp() {
        processingResult = ExpiredRentalProcessingDto.builder()
                .totalExpiredRentals(5)
                .processedSuccessfully(5)
                .failedToProcess(0)
                .build();
    }

    @Test
    @DisplayName("Should invoke expired rental processing")
    void shouldInvokeExpiredRentalProcessing() {
        when(expiredRentalProcessingService.processExpiredRentals())
                .thenReturn(processingResult);

        scheduler.processExpiredRentals();

        verify(expiredRentalProcessingService, times(1)).processExpiredRentals();
    }

    @Test
    @DisplayName("Should handle processing with no expired rentals")
    void shouldHandleProcessingWithNoExpiredRentals() {
        ExpiredRentalProcessingDto emptyResult = ExpiredRentalProcessingDto.builder()
                .totalExpiredRentals(0)
                .processedSuccessfully(0)
                .failedToProcess(0)
                .build();
        when(expiredRentalProcessingService.processExpiredRentals())
                .thenReturn(emptyResult);

        scheduler.processExpiredRentals();

        verify(expiredRentalProcessingService, times(1)).processExpiredRentals();
    }

    @Test
    @DisplayName("Should handle processing with failures")
    void shouldHandleProcessingWithFailures() {
        ExpiredRentalProcessingDto failureResult = ExpiredRentalProcessingDto.builder()
                .totalExpiredRentals(10)
                .processedSuccessfully(7)
                .failedToProcess(3)
                .build();
        when(expiredRentalProcessingService.processExpiredRentals())
                .thenReturn(failureResult);

        scheduler.processExpiredRentals();

        verify(expiredRentalProcessingService, times(1)).processExpiredRentals();
    }
}
