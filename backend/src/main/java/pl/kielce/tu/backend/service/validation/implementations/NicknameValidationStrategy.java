package pl.kielce.tu.backend.service.validation.implementations;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationConstraints;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;

@Slf4j
@Component
public class NicknameValidationStrategy implements FieldValidationStrategy<String> {

    @Override
    public void validate(String nickname) throws ValidationException {
        validateNotEmpty(nickname);
        validateLength(nickname);
        validatePattern(nickname);
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
