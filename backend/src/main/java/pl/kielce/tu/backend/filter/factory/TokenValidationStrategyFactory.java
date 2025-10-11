package pl.kielce.tu.backend.filter.factory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.kielce.tu.backend.filter.chain.ValidationChain;
import pl.kielce.tu.backend.filter.chain.ValidationStep;
import pl.kielce.tu.backend.filter.strategy.TokenPresenceInput;
import pl.kielce.tu.backend.filter.strategy.ValidationStrategy;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.constant.TokenValidationNames;

@Component
public class TokenValidationStrategyFactory {

    private final Map<TokenValidationNames, ValidationStrategy<?>> strategies;
    private final List<TokenValidationNames> executionOrder;

    @Autowired
    public TokenValidationStrategyFactory(List<ValidationStrategy<?>> validationStrategies) {
        this.strategies = new LinkedHashMap<>();
        validationStrategies.forEach(strategy -> strategies.put(strategy.getName(), strategy));
        this.executionOrder = List.of(
                TokenValidationNames.PRESENCE,
                TokenValidationNames.BLACKLIST,
                TokenValidationNames.EXTRACTION,
                TokenValidationNames.USER_EXISTENCE,
                TokenValidationNames.ADMIN_ACCESS);
    }

    public ValidationChain createValidationChain(HttpServletRequest request, HttpServletResponse response,
            String path, CookieNames tokenType) {
        List<ValidationStep> steps = new ArrayList<>();

        for (TokenValidationNames validationName : executionOrder) {
            ValidationStrategy<?> strategy = strategies.get(validationName);
            if (strategy == null)
                continue;

            ValidationStep step = createStep(strategy, validationName, request, response, path, tokenType);
            steps.add(step);
        }

        return new ValidationChain(steps);
    }

    @SuppressWarnings("unchecked")
    private <T> ValidationStep createStep(ValidationStrategy<?> strategy, TokenValidationNames name,
            HttpServletRequest request, HttpServletResponse response, String path, CookieNames tokenType) {

        if (name == TokenValidationNames.PRESENCE) {
            var typedStrategy = (ValidationStrategy<TokenPresenceInput>) strategy;
            return prev -> typedStrategy.validate(new TokenPresenceInput(request, tokenType), response, path);
        }

        return result -> {
            var typedStrategy = (ValidationStrategy<Object>) strategy;
            return typedStrategy.validate(result.getData(), response, path);
        };
    }
}
