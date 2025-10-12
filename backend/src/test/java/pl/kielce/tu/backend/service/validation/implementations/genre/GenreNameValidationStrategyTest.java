package pl.kielce.tu.backend.service.validation.implementations.genre;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.entity.Genre;
import pl.kielce.tu.backend.repository.GenreRepository;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class GenreNameValidationStrategyTest {

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private UserContextLogger userContextLogger;

    @InjectMocks
    private GenreNameValidationStrategy strategy;

    @Test
    void validate_success_callsLoggerStartedAndSuccess() throws ValidationException {
        String name = "Action";
        when(genreRepository.findByName(name)).thenReturn(Optional.empty());

        strategy.validate(name);

        String info = name.length() + " chars";
        InOrder inOrder = inOrder(userContextLogger);
        inOrder.verify(userContextLogger).logValidationOperation("GENRE_NAME", "STARTED", info);
        inOrder.verify(userContextLogger).logValidationOperation("GENRE_NAME", "SUCCESS", info);
    }

    @Test
    void validate_null_throwsValidationException_andLogsFailure() {
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(null));
        assertEquals("Genre name is required", ex.getMessage());

        InOrder inOrder = inOrder(userContextLogger);
        inOrder.verify(userContextLogger).logValidationOperation("GENRE_NAME", "STARTED", "null");
        inOrder.verify(userContextLogger).logValidationOperation("GENRE_NAME", "FAILURE", "Genre name is required");
    }

    @Test
    void validate_blank_throwsValidationException_andLogsFailure() {
        String name = "   ";
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(name));
        assertEquals("Genre name cannot be blank", ex.getMessage());

        String info = name.length() + " chars";
        InOrder inOrder = inOrder(userContextLogger);
        inOrder.verify(userContextLogger).logValidationOperation("GENRE_NAME", "STARTED", info);
        inOrder.verify(userContextLogger).logValidationOperation("GENRE_NAME", "FAILURE", "Genre name cannot be blank");
    }

    @Test
    void validate_tooShort_throwsValidationException_andLogsFailure() {
        String name = "Abcd";
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(name));
        assertEquals("Genre name must be at least 5 characters long", ex.getMessage());

        String info = name.length() + " chars";
        InOrder inOrder = inOrder(userContextLogger);
        inOrder.verify(userContextLogger).logValidationOperation("GENRE_NAME", "STARTED", info);
        inOrder.verify(userContextLogger).logValidationOperation("GENRE_NAME", "FAILURE",
                "Genre name must be at least 5 characters long");
    }

    @Test
    void validate_tooLong_throwsValidationException_andLogsFailure() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 76; i++) {
            sb.append('a');
        }
        String name = sb.toString();
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(name));
        assertEquals("Genre name cannot exceed 75 characters", ex.getMessage());

        String info = name.length() + " chars";
        InOrder inOrder = inOrder(userContextLogger);
        inOrder.verify(userContextLogger).logValidationOperation("GENRE_NAME", "STARTED", info);
        inOrder.verify(userContextLogger).logValidationOperation("GENRE_NAME", "FAILURE",
                "Genre name cannot exceed 75 characters");
    }

    @Test
    void validate_notUnique_throwsValidationException_andLogsFailure() {
        String name = "Action";
        when(genreRepository.findByName(anyString())).thenReturn(Optional.of(Mockito.mock(Genre.class)));

        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(name));
        assertEquals("Genre name already exists", ex.getMessage());

        String info = name.length() + " chars";
        InOrder inOrder = inOrder(userContextLogger);
        inOrder.verify(userContextLogger).logValidationOperation("GENRE_NAME", "STARTED", info);
        inOrder.verify(userContextLogger).logValidationOperation("GENRE_NAME", "FAILURE", "Genre name already exists");
    }
}
