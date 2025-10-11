package pl.kielce.tu.backend.service.validation.implementations;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationConstraints;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;

@Slf4j
@Component
public class PasswordValidationStrategy implements FieldValidationStrategy<String> {

    @Override
    public void validate(String password) throws ValidationException {
        validateNotEmpty(password);
        validateLength(password);
    }

    @Override
    public ValidationStrategyType getStrategyType() {
        return ValidationStrategyType.PASSWORD;
    }

    private void validateNotEmpty(String password) throws ValidationException {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Password cannot be empty");
        }
    }

    private void validateLength(String password) throws ValidationException {
        validateMinLength(password);
        validateMaxLength(password);
    }

    private void validateMinLength(String password) throws ValidationException {
        int minLength = ValidationConstraints.MIN_PASSWORD_LENGTH.getValue();
        if (password.length() < minLength) {
            throw new ValidationException(
                    String.format("Password must be at least %d characters long", minLength));
        }
    }

    private void validateMaxLength(String password) throws ValidationException {
        int maxLength = ValidationConstraints.MAX_PASSWORD_LENGTH.getValue();
        if (password.length() > maxLength) {
            throw new ValidationException(
                    String.format("Password cannot be longer than %d characters", maxLength));
        }
    }

}
