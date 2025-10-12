package pl.kielce.tu.backend.service.genre;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.model.dto.GenreDto;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;
import pl.kielce.tu.backend.service.validation.factory.ValidationStrategyFactory;

class GenreValidationServiceTest {

    private ValidationStrategyFactory validationFactory;
    private GenreValidationService service;

    private RecordingHandler nameHandler;
    private Object nameStrategyProxy;

    private RecordingHandler deletionHandler;
    private Object deletionStrategyProxy;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        validationFactory = Mockito.mock(ValidationStrategyFactory.class);
        prepareStrategyProxies();
        service = new GenreValidationService(validationFactory);

        Mockito.when(validationFactory.getStrategy(ValidationStrategyType.GENRE_NAME))
                .thenReturn((FieldValidationStrategy<Object>) nameStrategyProxy);
        Mockito.when(validationFactory.getStrategy(ValidationStrategyType.GENRE_DELETION))
                .thenReturn((FieldValidationStrategy<Object>) deletionStrategyProxy);
    }

    private void prepareStrategyProxies() {
        try {
            Method getStrategyMethod = ValidationStrategyFactory.class.getMethod("getStrategy",
                    ValidationStrategyType.class);
            Class<?> returnType = getStrategyMethod.getReturnType();

            nameHandler = new RecordingHandler();
            nameStrategyProxy = Proxy.newProxyInstance(
                    returnType.getClassLoader(),
                    new Class<?>[] { returnType },
                    nameHandler);

            deletionHandler = new RecordingHandler();
            deletionStrategyProxy = Proxy.newProxyInstance(
                    returnType.getClassLoader(),
                    new Class<?>[] { returnType },
                    deletionHandler);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to prepare strategy proxies for tests", e);
        }
    }

    @Test
    void validateForCreation_nullDto_throwsValidationException() {
        ValidationException ex = assertThrows(ValidationException.class, () -> service.validateForCreation(null));
        assertEquals("Genre data cannot be null", ex.getMessage());
    }

    @Test
    void validateForCreation_withName_delegatesToNameStrategy() throws ValidationException {
        GenreDto dto = Mockito.mock(GenreDto.class);
        Mockito.when(dto.getName()).thenReturn("Action");

        service.validateForCreation(dto);

        List<Object[]> calls = nameHandler.getInvocations();
        assertEquals(1, calls.size());
        Object[] args = calls.get(0);
        assertEquals(1, args.length);
        assertEquals("Action", args[0]);
    }

    @Test
    void validateForCreation_withNullName_doesNotCallNameStrategy() throws ValidationException {
        GenreDto dto = Mockito.mock(GenreDto.class);
        Mockito.when(dto.getName()).thenReturn(null);

        service.validateForCreation(dto);

        List<Object[]> calls = nameHandler.getInvocations();
        assertEquals(0, calls.size());
    }

    @Test
    void validateForDeletion_nullOrEmpty_throwsValidationException() {
        ValidationException ex1 = assertThrows(ValidationException.class, () -> service.validateForDeletion(null));
        assertEquals("Genre ID cannot be empty", ex1.getMessage());

        ValidationException ex2 = assertThrows(ValidationException.class, () -> service.validateForDeletion("   "));
        assertEquals("Genre ID cannot be empty", ex2.getMessage());
    }

    @Test
    void validateForDeletion_notANumber_throwsValidationException() {
        ValidationException ex = assertThrows(ValidationException.class, () -> service.validateForDeletion("12a"));
        assertEquals("Genre ID must be a valid number", ex.getMessage());
    }

    @Test
    void validateForDeletion_validId_delegatesToDeletionStrategy() throws ValidationException {
        service.validateForDeletion("123");

        List<Object[]> calls = deletionHandler.getInvocations();
        assertEquals(1, calls.size());
        Object[] args = calls.get(0);
        assertEquals(1, args.length);
        assertEquals(123L, args[0]);
    }

    private static class RecordingHandler implements InvocationHandler {
        private final List<Object[]> invocations = new ArrayList<>();

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("validate".equals(method.getName())) {
                invocations.add(args == null ? new Object[0] : args.clone());
                return null;
            }
            if ("toString".equals(method.getName())) {
                return proxy.getClass().getName() + "@" + System.identityHashCode(proxy);
            }
            if ("hashCode".equals(method.getName())) {
                return System.identityHashCode(proxy);
            }
            if ("equals".equals(method.getName()) && args != null && args.length == 1) {
                return proxy == args[0];
            }
            return null;
        }

        List<Object[]> getInvocations() {
            return invocations;
        }
    }
}
