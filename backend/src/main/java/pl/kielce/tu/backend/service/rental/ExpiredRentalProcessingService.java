package pl.kielce.tu.backend.service.rental;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.dto.ExpiredRentalProcessingDto;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.repository.RentalRepository;
import pl.kielce.tu.backend.service.rental.strategy.ReturnRequestStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@Service
@RequiredArgsConstructor
public class ExpiredRentalProcessingService {

    private final RentalRepository rentalRepository;
    private final UserContextLogger userContextLogger;
    private final ReturnRequestStrategy returnRequestStrategy;

    @Transactional
    public ExpiredRentalProcessingDto processExpiredRentals() {
        List<Rental> expiredRentals = findExpiredRentals();
        return processRentals(expiredRentals);
    }

    private List<Rental> findExpiredRentals() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        return rentalRepository.findExpiredActiveRentals(currentDateTime);
    }

    private ExpiredRentalProcessingDto processRentals(List<Rental> rentals) {
        int successCount = 0;
        int failureCount = 0;

        for (Rental rental : rentals) {
            if (processRental(rental)) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        return buildProcessingResult(rentals.size(), successCount, failureCount);
    }

    private boolean processRental(Rental rental) {
        try {
            if (returnRequestStrategy.canProcess(rental)) {
                returnRequestStrategy.processReturnRequest(rental);
                logSuccessfulProcessing(rental);
                return true;
            }
            logSkippedRental(rental);
            return false;
        } catch (Exception e) {
            logFailedProcessing(rental, e);
            return false;
        }
    }

    private ExpiredRentalProcessingDto buildProcessingResult(
            int total, int success, int failure) {
        return ExpiredRentalProcessingDto.builder()
                .totalExpiredRentals(total)
                .processedSuccessfully(success)
                .failedToProcess(failure)
                .build();
    }

    private void logSuccessfulProcessing(Rental rental) {
        userContextLogger.logUserOperation("EXPIRED_RENTAL_PROCESSED",
                "Rental ID: " + rental.getId());
    }

    private void logSkippedRental(Rental rental) {
        userContextLogger.logUserOperation("EXPIRED_RENTAL_SKIPPED",
                "Rental ID: " + rental.getId() + " - Cannot process");
    }

    private void logFailedProcessing(Rental rental, Exception e) {
        userContextLogger.logUserOperation("EXPIRED_RENTAL_FAILED",
                "Rental ID: " + rental.getId() + " - " + e.getMessage());
    }

}
