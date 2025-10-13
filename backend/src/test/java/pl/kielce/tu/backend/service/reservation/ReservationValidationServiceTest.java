package pl.kielce.tu.backend.service.reservation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.model.dto.ReservationDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;
import pl.kielce.tu.backend.service.validation.factory.ValidationStrategyFactory;

@ExtendWith(MockitoExtension.class)
class ReservationValidationServiceTest {

    @Mock
    private ValidationStrategyFactory validationFactory;

    @Mock
    private FieldValidationStrategy<Object> mockStrategy;

    private ReservationValidationService service;

    @BeforeEach
    void setUp() {
        service = new ReservationValidationService(validationFactory);
        when(validationFactory.getStrategy(any(ValidationStrategyType.class))).thenReturn(mockStrategy);
    }

    @Test
    void validateReservationRequest_validData_callsBothValidations() throws ValidationException {
        ReservationDto reservationDto = createValidReservationDto();
        Dvd dvd = createAvailableDvd();
        service.validateReservationRequest(reservationDto, dvd);

        verify(validationFactory).getStrategy(ValidationStrategyType.RESERVATION_DVD_AVAILABILITY);
        verify(validationFactory).getStrategy(ValidationStrategyType.RESERVATION_COPY_COUNT);
        verify(mockStrategy, times(2)).validate(any());
    }

    @Test
    void validateDvdAvailability_validDvd_callsStrategy() throws ValidationException {
        Dvd dvd = createAvailableDvd();
        service.validateDvdAvailability(dvd);

        verify(validationFactory).getStrategy(ValidationStrategyType.RESERVATION_DVD_AVAILABILITY);
        verify(mockStrategy).validate(dvd);
    }

    @Test
    void validateCopyCount_validData_callsStrategy() throws ValidationException {
        ReservationDto reservationDto = createValidReservationDto();
        Dvd dvd = createAvailableDvd();
        service.validateCopyCount(reservationDto, dvd);

        verify(validationFactory).getStrategy(ValidationStrategyType.RESERVATION_COPY_COUNT);
        verify(mockStrategy).validate(any());
    }

    @Test
    void validateReservationRequest_validationThrows_propagatesException() throws ValidationException {
        ReservationDto reservationDto = createValidReservationDto();
        Dvd dvd = createAvailableDvd();
        doThrow(new ValidationException("Test validation error")).when(mockStrategy).validate(any());

        ValidationException exception = assertThrows(ValidationException.class,
                () -> service.validateReservationRequest(reservationDto, dvd));
        assertEquals("Test validation error", exception.getMessage());
    }

    private ReservationDto createValidReservationDto() {
        ReservationDto dto = new ReservationDto();
        dto.setCount(2);
        dto.setDvdId(1L);
        return dto;
    }

    private Dvd createAvailableDvd() {
        Dvd dvd = new Dvd();
        dvd.setId(1L);
        dvd.setAvalaible(true);
        dvd.setCopiesAvalaible(10);
        return dvd;
    }
}
