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
public class PasswordValidationStrategy implements FieldValidationStrategy<String> {

    private final UserContextLogger userContextLogger;

    @Override
    public void validate(String password) throws ValidationException {
        try {
            validateNotEmpty(password);
            validateLength(password);
            userContextLogger.logValidationOperation("PASSWORD", "SUCCESS", "Password validation passed");
        } catch (ValidationException e) {
            userContextLogger.logValidationOperation("PASSWORD", "FAILURE", e.getMessage());
            throw e;
        }
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
