package pl.kielce.tu.backend.service.dvd;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.repository.DvdRepository;
import pl.kielce.tu.backend.util.UserContextLogger;

@Service
@RequiredArgsConstructor
public class DvdAvailabilityService {

    private final DvdRepository dvdRepository;
    private final UserContextLogger userContextLogger;

    @Transactional
    public void decreaseAvailability(Dvd dvd, Integer count) {
        logAvailabilityOperation("DECREASE_STARTED", dvd, count);

        validateAvailabilityForDecrease(dvd, count);
        updateCopiesCount(dvd, -count);
        updateAvailabilityStatus(dvd);
        saveDvd(dvd);

        logAvailabilityOperation("DECREASE_COMPLETED", dvd, count);
    }

    @Transactional
    public void increaseAvailability(Dvd dvd, Integer count) {
        logAvailabilityOperation("INCREASE_STARTED", dvd, count);

        updateCopiesCount(dvd, count);
        updateAvailabilityStatus(dvd);
        saveDvd(dvd);

        logAvailabilityOperation("INCREASE_COMPLETED", dvd, count);
    }

    private void logAvailabilityOperation(String operation, Dvd dvd, Integer count) {
        String message = "DVD ID: " + dvd.getId() + ", Count: " + count +
                ", Current copies: " + dvd.getCopiesAvalaible();
        userContextLogger.logUserOperation("DVD_AVAILABILITY_" + operation, message);
    }

    private void validateAvailabilityForDecrease(Dvd dvd, Integer count) {
        if (dvd.getCopiesAvalaible() < count) {
            throw new IllegalStateException("Insufficient copies available. Available: " +
                    dvd.getCopiesAvalaible() + ", Requested: " + count);
        }
    }

    private void updateCopiesCount(Dvd dvd, Integer delta) {
        int newCount = dvd.getCopiesAvalaible() + delta;
        dvd.setCopiesAvalaible(newCount);
    }

    private void updateAvailabilityStatus(Dvd dvd) {
        boolean hasAvailableCopies = dvd.getCopiesAvalaible() > 0;
        dvd.setAvalaible(hasAvailableCopies);
    }

    private void saveDvd(Dvd dvd) {
        dvdRepository.save(dvd);
    }

}
