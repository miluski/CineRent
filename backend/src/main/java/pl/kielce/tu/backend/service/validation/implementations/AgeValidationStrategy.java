package pl.kielce.tu.backend.service.validation.implementations;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationConstraints;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;

@Slf4j
@Component
public class AgeValidationStrategy implements FieldValidationStrategy<Integer> {

    @Override
    public void validate(Integer age) throws ValidationException {
        validateNotNull(age);
        validateAgeRange(age);
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
