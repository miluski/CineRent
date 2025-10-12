package pl.kielce.tu.backend.service.validation.implementations.genre;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.repository.DvdRepository;
import pl.kielce.tu.backend.repository.GenreRepository;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class GenreDeletionValidationStrategyTest {

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private DvdRepository dvdRepository;

    @Mock
    private UserContextLogger userContextLogger;

    @InjectMocks
    private GenreDeletionValidationStrategy strategy;

    @Test
    void getStrategyType_shouldReturnGenreDeletion() {
        assertEquals(ValidationStrategyType.GENRE_DELETION, strategy.getStrategyType());
    }

    @Test
    void validate_nullId_shouldThrowValidationException() {
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(null));
        assertEquals("Genre ID is required", ex.getMessage());

        InOrder inOrder = inOrder(userContextLogger);
        inOrder.verify(userContextLogger).logValidationOperation("GENRE_DELETION", "STARTED", "null");
        inOrder.verify(userContextLogger).logValidationOperation("GENRE_DELETION", "FAILURE", "Genre ID is required");
    }

    @Test
    void validate_genreNotExists_shouldThrowValidationException() {
        Long genreId = 999L;
        when(genreRepository.existsById(genreId)).thenReturn(false);

        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(genreId));
        assertEquals("Genre not found with ID: 999", ex.getMessage());
    }

    @Test
    void validate_minimumGenreCount_shouldThrowValidationException() {
        Long genreId = 1L;
        when(genreRepository.existsById(genreId)).thenReturn(true);
        when(genreRepository.count()).thenReturn(2L);

        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(genreId));
        assertEquals("Cannot delete genre: minimum 2 genres must remain in database", ex.getMessage());
    }

    @Test
    void validate_genreAssignedToDvds_shouldThrowValidationException() {
        Long genreId = 1L;
        when(genreRepository.existsById(genreId)).thenReturn(true);
        when(genreRepository.count()).thenReturn(5L);
        when(dvdRepository.existsByGenresId(genreId)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(genreId));
        assertEquals("Cannot delete genre: it is assigned to one or more DVDs", ex.getMessage());
    }

    @Test
    void validate_validDeletion_shouldNotThrow() throws ValidationException {
        Long genreId = 1L;
        when(genreRepository.existsById(genreId)).thenReturn(true);
        when(genreRepository.count()).thenReturn(5L);
        when(dvdRepository.existsByGenresId(genreId)).thenReturn(false);

        assertDoesNotThrow(() -> strategy.validate(genreId));

        InOrder inOrder = inOrder(userContextLogger);
        inOrder.verify(userContextLogger).logValidationOperation("GENRE_DELETION", "STARTED", "1");
        inOrder.verify(userContextLogger).logValidationOperation("GENRE_DELETION", "SUCCESS", "1");
    }

    @Test
    void validate_exactlyThreeGenres_shouldAllowDeletion() throws ValidationException {
        Long genreId = 1L;
        when(genreRepository.existsById(genreId)).thenReturn(true);
        when(genreRepository.count()).thenReturn(3L);
        when(dvdRepository.existsByGenresId(genreId)).thenReturn(false);

        assertDoesNotThrow(() -> strategy.validate(genreId));
    }

}
