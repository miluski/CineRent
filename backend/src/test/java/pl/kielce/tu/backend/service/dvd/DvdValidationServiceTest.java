package pl.kielce.tu.backend.service.dvd;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.model.dto.DvdDto;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;
import pl.kielce.tu.backend.service.validation.factory.ValidationStrategyFactory;

@ExtendWith(MockitoExtension.class)
class DvdValidationServiceTest {

    @Mock
    private ValidationStrategyFactory validationFactory;

    @Mock
    private FieldValidationStrategy<Object> validationStrategy;

    private DvdValidationService service;

    @BeforeEach
    void setUp() {
        service = new DvdValidationService(validationFactory);
    }

    @Test
    void validateForCreation_nullDto_throwsValidationException() {
        assertThrows(ValidationException.class, () -> service.validateForCreation(null));
        verifyNoInteractions(validationFactory);
    }

    @Test
    void validateForCreation_allFieldsProvided_callsAllStrategies() throws ValidationException {
        when(validationFactory.getStrategy(any(ValidationStrategyType.class))).thenReturn(validationStrategy);
        DvdDto dto = new DvdDto();
        dto.setTitle("Title");
        dto.setGenresIdentifiers(Collections.singletonList(1L));
        dto.setReleaseYear(2021);
        dto.setDirectors(Collections.singletonList("Director"));
        dto.setDescription("Desc");
        dto.setDurationMinutes(120);
        dto.setCopiesAvailable(3);
        dto.setRentalPricePerDay(4.5f);
        assertDoesNotThrow(() -> service.validateForCreation(dto));
        verify(validationFactory, atLeastOnce()).getStrategy(ValidationStrategyType.DVD_TITLE);
        verify(validationFactory, atLeastOnce()).getStrategy(ValidationStrategyType.GENRE);
        verify(validationFactory, atLeastOnce()).getStrategy(ValidationStrategyType.DVD_YEAR);
        verify(validationFactory, atLeastOnce()).getStrategy(ValidationStrategyType.DVD_DIRECTORS);
        verify(validationFactory, atLeastOnce()).getStrategy(ValidationStrategyType.DVD_DESCRIPTION);
        verify(validationFactory, atLeastOnce()).getStrategy(ValidationStrategyType.DVD_DURATION);
        verify(validationFactory, atLeastOnce()).getStrategy(ValidationStrategyType.DVD_COPIES);
        verify(validationFactory, atLeastOnce()).getStrategy(ValidationStrategyType.DVD_PRICE);
        verify(validationStrategy, times(8)).validate(any());
    }

    @Test
    void validateForUpdate_noFieldsProvided_throwsValidationException() {
        DvdDto dto = new DvdDto();
        assertThrows(ValidationException.class, () -> service.validateForUpdate(dto));
        verifyNoInteractions(validationFactory);
    }

    @Test
    void validateForUpdate_someFieldProvided_doesNotThrow() {
        DvdDto dto = new DvdDto();
        dto.setTitle("Non empty");
        assertDoesNotThrow(() -> service.validateForUpdate(dto));
        verifyNoInteractions(validationFactory);
    }

    @Test
    void validateTitle_null_doesNotCallFactory() throws ValidationException {
        service.validateTitle(null);
        verifyNoInteractions(validationFactory);
    }

    @Test
    void validateTitle_nonNull_callsCorrectStrategyAndValidate() throws ValidationException {
        when(validationFactory.getStrategy(ValidationStrategyType.DVD_TITLE)).thenReturn(validationStrategy);
        String title = "Some title";
        service.validateTitle(title);
        verify(validationFactory).getStrategy(ValidationStrategyType.DVD_TITLE);
        verify(validationStrategy).validate(title);
    }

    @Test
    void validateGenres_null_doesNotCallFactory() throws ValidationException {
        service.validateGenres(null);
        verifyNoInteractions(validationFactory);
    }

    @Test
    void validateGenres_nonNull_callsCorrectStrategyAndValidate() throws ValidationException {
        when(validationFactory.getStrategy(ValidationStrategyType.GENRE)).thenReturn(validationStrategy);
        Object genres = Collections.singletonList(1L);
        service.validateGenres(genres);
        verify(validationFactory).getStrategy(ValidationStrategyType.GENRE);
        verify(validationStrategy).validate(genres);
    }
}
