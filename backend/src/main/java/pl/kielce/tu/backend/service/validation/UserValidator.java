package pl.kielce.tu.backend.service.validation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.RankType;
import pl.kielce.tu.backend.model.constant.ValidationConstraints;
import pl.kielce.tu.backend.model.dto.UserDto;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserValidator implements Validator<UserDto> {

    private final UserRepository userRepository;

    @Override
    public void validate(UserDto userDto) throws ValidationException {
        if (userDto == null) {
            throw new ValidationException("User data cannot be null");
        }

        validateNickname(userDto.getNickname());
        validatePassword(userDto.getPassword());
    }

    public void validateUserIsAdmin(Long userId) throws ValidationException {
        validateUserExists(userId);

        User user = findUserById(userId);
        if (user.getRank() != RankType.ADMIN) {
            log.warn("Admin validation failed: User with ID {} is not an admin", userId);
            throw new ValidationException();
        }
    }

    public void validateUserExists(Long userId) throws ValidationException {
        if (userId == null) {
            log.warn("User validation failed: User ID is null");
            throw new ValidationException();
        }

        User user = findUserById(userId);
        if (user == null) {
            log.warn("User validation failed: User with ID {} does not exist", userId);
            throw new ValidationException();
        }
    }

    private void validateNickname(String nickname) throws ValidationException {
        validateNotEmpty(nickname, "Nickname");
        validateLength(nickname, "Nickname",
                ValidationConstraints.MIN_NICKNAME_LENGTH.getValue(),
                ValidationConstraints.MAX_NICKNAME_LENGTH.getValue());
        validatePattern(nickname, "^[a-zA-Z0-9_-]+$",
                "Nickname can only contain letters, numbers, underscores, and hyphens");
    }

    private void validatePassword(String password) throws ValidationException {
        validateNotEmpty(password, "Password");
        validateLength(password, "Password",
                ValidationConstraints.MIN_PASSWORD_LENGTH.getValue(),
                ValidationConstraints.MAX_PASSWORD_LENGTH.getValue());
    }

    private void validateNotEmpty(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty");
        }
    }

    private void validateLength(String value, String fieldName, int minLength, int maxLength)
            throws ValidationException {
        if (value.length() < minLength) {
            throw new ValidationException(
                    String.format("%s must be at least %d characters long", fieldName, minLength));
        }
        if (value.length() > maxLength) {
            throw new ValidationException(
                    String.format("%s cannot be longer than %d characters", fieldName, maxLength));
        }
    }

    private void validatePattern(String value, String regex, String errorMessage)
            throws ValidationException {
        if (!value.matches(regex)) {
            throw new ValidationException(errorMessage);
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

}
