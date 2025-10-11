package pl.kielce.tu.backend.service.validation;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;

public interface FieldValidationStrategy<T> {

    void validate(T value) throws ValidationException;

    ValidationStrategyType getStrategyType();

}
