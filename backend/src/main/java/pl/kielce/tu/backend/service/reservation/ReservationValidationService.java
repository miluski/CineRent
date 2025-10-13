package pl.kielce.tu.backend.service.reservation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.model.dto.ReservationCancellationRequest;
import pl.kielce.tu.backend.model.dto.ReservationDto;
import pl.kielce.tu.backend.model.dto.ReservationValidationRequest;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.Reservation;
import pl.kielce.tu.backend.service.validation.factory.ValidationStrategyFactory;

@Component
@RequiredArgsConstructor
public class ReservationValidationService {

    private final ValidationStrategyFactory validationFactory;

    public void validateReservationRequest(ReservationDto reservationDto, Dvd dvd) throws ValidationException {
        validateDvdAvailability(dvd);
        validateCopyCount(reservationDto, dvd);
    }

    public void validateDvdAvailability(Dvd dvd) throws ValidationException {
        validationFactory.getStrategy(ValidationStrategyType.RESERVATION_DVD_AVAILABILITY)
                .validate(dvd);
    }

    public void validateCopyCount(ReservationDto reservationDto, Dvd dvd) throws ValidationException {
        ReservationValidationRequest request = ReservationValidationRequest.builder()
                .reservation(reservationDto)
                .dvd(dvd)
                .build();

        validationFactory.getStrategy(ValidationStrategyType.RESERVATION_COPY_COUNT)
                .validate(request);
    }

    public void validateReservationCancellation(Reservation reservation, Long userId) throws ValidationException {
        ReservationCancellationRequest request = ReservationCancellationRequest.builder()
                .reservation(reservation)
                .userId(userId)
                .build();

        validationFactory.getStrategy(ValidationStrategyType.RESERVATION_CANCELLATION)
                .validate(request);
    }

}
