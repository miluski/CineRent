package pl.kielce.tu.backend.service.validation.implementations.user;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationConstraints;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class AgeValidationStrategy implements FieldValidationStrategy<Integer> {

    private final UserContextLogger userContextLogger;

    @Override
    public void validate(Integer age) throws ValidationException {
        try {
            validateNotNull(age);
            validateAgeRange(age);
            userContextLogger.logValidationOperation("AGE", "SUCCESS", "Age validation passed for value: " + age);
        } catch (ValidationException e) {
            userContextLogger.logValidationOperation("AGE", "FAILURE", e.getMessage());
            throw e;
        }
    }

    @Override
    public ValidationStrategyType getStrategyType() {
        return ValidationStrategyType.AGE;
    }

    private void validateNotNull(Integer age) throws ValidationException {
        if (age == null) {
            throw new ValidationException("Age cannot be null");
        }
    }

    private void validateAgeRange(Integer age) throws ValidationException {
        int minAge = ValidationConstraints.MIN_AGE.getValue();
        int maxAge = ValidationConstraints.MAX_AGE.getValue();

        if (age < minAge || age > maxAge) {
            throw new ValidationException(
                    String.format("Age must be between %d and %d", minAge, maxAge));
        }
    }

}
