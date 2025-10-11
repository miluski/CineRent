package pl.kielce.tu.backend.service.validation.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;

class ValidationStrategyFactoryTest {

    private ValidationStrategyFactory factory;

    @SuppressWarnings("rawtypes")
    private FieldValidationStrategy nicknameStrategy;
    @SuppressWarnings("rawtypes")
    private FieldValidationStrategy passwordStrategy;
    @SuppressWarnings("rawtypes")
    private FieldValidationStrategy ageStrategy;
    @SuppressWarnings("rawtypes")
    private FieldValidationStrategy genreStrategy;

    @BeforeEach
    void setUp() {
        nicknameStrategy = mock(FieldValidationStrategy.class);
        passwordStrategy = mock(FieldValidationStrategy.class);
        ageStrategy = mock(FieldValidationStrategy.class);
        genreStrategy = mock(FieldValidationStrategy.class);

        when(nicknameStrategy.getStrategyType()).thenReturn(ValidationStrategyType.NICKNAME);
        when(passwordStrategy.getStrategyType()).thenReturn(ValidationStrategyType.PASSWORD);
        when(ageStrategy.getStrategyType()).thenReturn(ValidationStrategyType.AGE);
        when(genreStrategy.getStrategyType()).thenReturn(ValidationStrategyType.GENRE);

        List<FieldValidationStrategy<?>> strategies = Arrays.asList(
                nicknameStrategy,
                passwordStrategy,
                ageStrategy,
                genreStrategy);

        factory = new ValidationStrategyFactory(strategies);
        factory.initializeStrategies();
    }

    @Test
    void initializeStrategies_shouldPopulateStrategyMap() {
        assertNotNull(factory.getStrategy(ValidationStrategyType.NICKNAME));
        assertNotNull(factory.getStrategy(ValidationStrategyType.PASSWORD));
        assertNotNull(factory.getStrategy(ValidationStrategyType.AGE));
        assertNotNull(factory.getStrategy(ValidationStrategyType.GENRE));
    }

    @Test
    void getStrategy_shouldReturnCorrectStrategy_forNickname() {
        FieldValidationStrategy<?> strategy = factory.getStrategy(ValidationStrategyType.NICKNAME);

        assertEquals(nicknameStrategy, strategy);
        assertEquals(ValidationStrategyType.NICKNAME, strategy.getStrategyType());
    }

    @Test
    void getStrategy_shouldReturnCorrectStrategy_forPassword() {
        FieldValidationStrategy<?> strategy = factory.getStrategy(ValidationStrategyType.PASSWORD);

        assertEquals(passwordStrategy, strategy);
        assertEquals(ValidationStrategyType.PASSWORD, strategy.getStrategyType());
    }

    @Test
    void getStrategy_shouldReturnCorrectStrategy_forAge() {
        FieldValidationStrategy<?> strategy = factory.getStrategy(ValidationStrategyType.AGE);

        assertEquals(ageStrategy, strategy);
        assertEquals(ValidationStrategyType.AGE, strategy.getStrategyType());
    }

    @Test
    void getStrategy_shouldReturnCorrectStrategy_forGenre() {
        FieldValidationStrategy<?> strategy = factory.getStrategy(ValidationStrategyType.GENRE);

        assertEquals(genreStrategy, strategy);
        assertEquals(ValidationStrategyType.GENRE, strategy.getStrategyType());
    }

    @Test
    void getStrategy_shouldThrowException_whenStrategyTypeNotFound() {
        factory = new ValidationStrategyFactory(Collections.emptyList());
        factory.initializeStrategies();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.getStrategy(ValidationStrategyType.NICKNAME));

        assertEquals("No validation strategy found for type: NICKNAME", exception.getMessage());
    }

    @Test
    void getStrategy_shouldThrowException_whenStrategyTypeIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.getStrategy(null));

        assertEquals("No validation strategy found for type: null", exception.getMessage());
    }

    @Test
    void factory_shouldHandleEmptyStrategyList() {
        factory = new ValidationStrategyFactory(Collections.emptyList());
        factory.initializeStrategies();

        for (ValidationStrategyType type : ValidationStrategyType.values()) {
            assertThrows(IllegalArgumentException.class, () -> factory.getStrategy(type));
        }
    }

    @Test
    void initializeStrategies_shouldOverwriteDuplicates_whenMultipleStrategiesForSameType() {
        @SuppressWarnings("unchecked")
        FieldValidationStrategy<String> duplicateNicknameStrategy = mock(FieldValidationStrategy.class);
        when(duplicateNicknameStrategy.getStrategyType()).thenReturn(ValidationStrategyType.NICKNAME);

        List<FieldValidationStrategy<?>> strategies = Arrays.asList(
                nicknameStrategy,
                duplicateNicknameStrategy);

        factory = new ValidationStrategyFactory(strategies);
        factory.initializeStrategies();

        FieldValidationStrategy<?> result = factory.getStrategy(ValidationStrategyType.NICKNAME);
        assertEquals(duplicateNicknameStrategy, result);
    }
}
