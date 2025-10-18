package pl.kielce.tu.backend.service.rental.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.dto.ExpiredRentalProcessingDto;
import pl.kielce.tu.backend.service.rental.ExpiredRentalProcessingService;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
@Tag(name = "Rental Scheduler", description = "Automated rental processing tasks")
public class RentalScheduler {

    private final UserContextLogger userContextLogger;
    private final ExpiredRentalProcessingService expiredRentalProcessingService;

    @Scheduled(cron = "0 */5 * * * *")
    @Operation(summary = "Process expired rentals", description = "Automatically creates return requests for rentals past their end date")
    public void processExpiredRentals() {
        userContextLogger.logUserOperation("SCHEDULED_EXPIRED_RENTAL_CHECK", "Starting expired rental processing");
        ExpiredRentalProcessingDto result = expiredRentalProcessingService.processExpiredRentals();
        logProcessingResults(result);
    }

    private void logProcessingResults(ExpiredRentalProcessingDto result) {
        String details = String.format("Total: %d, Success: %d, Failed: %d",
                result.getTotalExpiredRentals(),
                result.getProcessedSuccessfully(),
                result.getFailedToProcess());
        userContextLogger.logUserOperation("EXPIRED_RENTAL_PROCESSING_COMPLETED", details);
    }

}
