package pl.kielce.tu.backend.service.validation.implementations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.repository.GenreRepository;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class GenreValidationStrategyTest {

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private UserContextLogger userContextLogger;

    private GenreValidationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new GenreValidationStrategy(genreRepository, userContextLogger);
    }

    @Test
    void validate_shouldNotThrow_whenGenreIdsIsNull() {
        assertDoesNotThrow(() -> strategy.validate(null));
    }

    @Test
    void validate_shouldNotThrow_whenGenreIdsIsEmpty() {
        assertDoesNotThrow(() -> strategy.validate(Collections.emptyList()));
    }

    @Test
    void validate_shouldNotThrow_whenAllGenresExist() throws ValidationException {
        List<Long> ids = Arrays.asList(1L, 2L);
        when(genreRepository.existsById(1L)).thenReturn(true);
        when(genreRepository.existsById(2L)).thenReturn(true);

        assertDoesNotThrow(() -> strategy.validate(ids));

        verify(genreRepository, times(1)).existsById(1L);
        verify(genreRepository, times(1)).existsById(2L);
    }

    @Test
    void validate_shouldThrow_whenGenreIdIsNull() {
        List<Long> ids = Arrays.asList(1L, null, 3L);
        when(genreRepository.existsById(1L)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(ids));
        assertEquals("Genre identifier cannot be null", ex.getMessage());
    }

    @Test
    void validate_shouldThrow_whenGenreDoesNotExistInDatabase() {
        List<Long> ids = Arrays.asList(1L, 2L);
        when(genreRepository.existsById(1L)).thenReturn(true);
        when(genreRepository.existsById(2L)).thenReturn(false);

        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(ids));
        assertEquals("Genre with identifier 2 does not exist", ex.getMessage());
    }

    @Test
    void getStrategyType_shouldReturnGenre() {
        assertEquals(ValidationStrategyType.GENRE, strategy.getStrategyType());
    }
}
