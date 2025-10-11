package pl.kielce.tu.backend.service.validation.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;

@Component
@RequiredArgsConstructor
public class ValidationStrategyFactory {

    private final List<FieldValidationStrategy<?>> validationStrategies;
    private final Map<ValidationStrategyType, FieldValidationStrategy<?>> strategyMap = new HashMap<>();

    @PostConstruct
    public void initializeStrategies() {
        validationStrategies.forEach(strategy -> strategyMap.put(strategy.getStrategyType(), strategy));
    }

    @SuppressWarnings("unchecked")
    public <T> FieldValidationStrategy<T> getStrategy(ValidationStrategyType type) {
        FieldValidationStrategy<?> strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "No validation strategy found for type: " + type);
        }
        return (FieldValidationStrategy<T>) strategy;
    }

}
