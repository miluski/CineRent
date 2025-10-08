package pl.kielce.tu.backend.filter.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.kielce.tu.backend.filter.chain.ValidationChain;
import pl.kielce.tu.backend.filter.chain.ValidationStep;
import pl.kielce.tu.backend.filter.strategy.TokenPresenceInput;
import pl.kielce.tu.backend.filter.strategy.ValidationResult;
import pl.kielce.tu.backend.filter.strategy.ValidationStrategy;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.constant.TokenValidationNames;

class ValidationStrategyFactoryTest {

    @Test
    void createsStepsForAllExecutionOrderStrategies() throws Exception {
        ValidationStrategy<?> presence = mock(ValidationStrategy.class);
        ValidationStrategy<?> blacklist = mock(ValidationStrategy.class);
        ValidationStrategy<?> extraction = mock(ValidationStrategy.class);
        ValidationStrategy<?> userExistence = mock(ValidationStrategy.class);
        ValidationStrategy<?> adminAccess = mock(ValidationStrategy.class);

        when(presence.getName()).thenReturn(TokenValidationNames.PRESENCE);
        when(blacklist.getName()).thenReturn(TokenValidationNames.BLACKLIST);
        when(extraction.getName()).thenReturn(TokenValidationNames.EXTRACTION);
        when(userExistence.getName()).thenReturn(TokenValidationNames.USER_EXISTENCE);
        when(adminAccess.getName()).thenReturn(TokenValidationNames.ADMIN_ACCESS);

        ValidationStrategyFactory factory = new ValidationStrategyFactory(
                List.of(presence, blacklist, extraction, userExistence, adminAccess));

        ValidationChain chain = factory.createValidationChain(
                mock(HttpServletRequest.class),
                mock(HttpServletResponse.class),
                "/some/path",
                CookieNames.ACCESS_TOKEN);

        Field stepsField = Arrays.stream(chain.getClass().getDeclaredFields())
                .filter(f -> List.class.isAssignableFrom(f.getType()))
                .findFirst()
                .orElseThrow();
        stepsField.setAccessible(true);
        List<?> steps = (List<?>) stepsField.get(chain);

        assertEquals(5, steps.size());
    }

    @Test
    void presenceStepInvokesPresenceStrategyWithTokenPresenceInput() throws Exception {
        @SuppressWarnings("unchecked")
        ValidationStrategy<TokenPresenceInput> presence = (ValidationStrategy<TokenPresenceInput>) mock(
                ValidationStrategy.class);
        when(presence.getName()).thenReturn(TokenValidationNames.PRESENCE);
        when(presence.validate(any(TokenPresenceInput.class), any(HttpServletResponse.class), anyString()))
                .thenReturn(null);

        ValidationStrategyFactory factory = new ValidationStrategyFactory(List.of(presence));

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        String path = "/p";
        CookieNames tokenType = CookieNames.ACCESS_TOKEN;

        ValidationChain chain = factory.createValidationChain(req, resp, path, tokenType);

        Field stepsField = Arrays.stream(chain.getClass().getDeclaredFields())
                .filter(f -> List.class.isAssignableFrom(f.getType()))
                .findFirst()
                .orElseThrow();
        stepsField.setAccessible(true);
        List<?> steps = (List<?>) stepsField.get(chain);

        ValidationStep step = (ValidationStep) steps.get(0);

        step.execute();

        verify(presence).validate(any(TokenPresenceInput.class), any(HttpServletResponse.class), anyString());
    }

    @Test
    void nonPresenceStepInvokesStrategyWithPrevData() throws Exception {
        @SuppressWarnings("unchecked")
        ValidationStrategy<Object> blacklist = (ValidationStrategy<Object>) mock(ValidationStrategy.class);
        when(blacklist.getName()).thenReturn(TokenValidationNames.BLACKLIST);
        when(blacklist.validate(any(), any(HttpServletResponse.class), anyString())).thenReturn(null);

        ValidationStrategyFactory factory = new ValidationStrategyFactory(List.of(blacklist));

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        String path = "/p";
        CookieNames tokenType = CookieNames.ACCESS_TOKEN;

        ValidationChain chain = factory.createValidationChain(req, resp, path, tokenType);

        Field stepsField = Arrays.stream(chain.getClass().getDeclaredFields())
                .filter(f -> List.class.isAssignableFrom(f.getType()))
                .findFirst()
                .orElseThrow();
        stepsField.setAccessible(true);
        List<?> steps = (List<?>) stepsField.get(chain);

        ValidationStep step = (ValidationStep) steps.get(0);

        ValidationResult prev = new ValidationResult(true, "TOKEN_FROM_PREV");
        step.execute(prev);

        verify(blacklist).validate("TOKEN_FROM_PREV", resp, path);
    }
}
