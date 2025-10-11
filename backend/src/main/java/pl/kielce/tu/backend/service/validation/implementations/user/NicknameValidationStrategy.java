package pl.kielce.tu.backend.service.validation.implementations.user;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationConstraints;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@Slf4j
@Component
@RequiredArgsConstructor
public class NicknameValidationStrategy implements FieldValidationStrategy<String> {

    private final UserContextLogger userContextLogger;

    @Override
    public void validate(String nickname) throws ValidationException {
        try {
            validateNotEmpty(nickname);
            validateLength(nickname);
            validatePattern(nickname);
            userContextLogger.logValidationOperation("NICKNAME", "SUCCESS", "Nickname validation passed");
        } catch (ValidationException e) {
            userContextLogger.logValidationOperation("NICKNAME", "FAILURE", e.getMessage());
            throw e;
        }
    }

    @Override
    public ValidationStrategyType getStrategyType() {
        return ValidationStrategyType.NICKNAME;
    }

    private void validateNotEmpty(String nickname) throws ValidationException {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new ValidationException("Nickname cannot be empty");
        }
    }

    private void validateLength(String nickname) throws ValidationException {
        validateMinLength(nickname);
        validateMaxLength(nickname);
    }

    private void validateMinLength(String nickname) throws ValidationException {
        int minLength = ValidationConstraints.MIN_NICKNAME_LENGTH.getValue();
        if (nickname.length() < minLength) {
            throw new ValidationException(
                    String.format("Nickname must be at least %d characters long", minLength));
        }
    }

    private void validateMaxLength(String nickname) throws ValidationException {
        int maxLength = ValidationConstraints.MAX_NICKNAME_LENGTH.getValue();
        if (nickname.length() > maxLength) {
            throw new ValidationException(
                    String.format("Nickname cannot be longer than %d characters", maxLength));
        }
    }

    private void validatePattern(String nickname) throws ValidationException {
        String pattern = ValidationConstraints.NICKNAME_PATTERN.getPattern();
        if (!nickname.matches(pattern)) {
            throw new ValidationException(
                    "Nickname can only contain letters, numbers, underscores, and hyphens");
        }
    }

}
