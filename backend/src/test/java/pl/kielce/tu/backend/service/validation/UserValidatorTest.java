package pl.kielce.tu.backend.service.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.RankType;
import pl.kielce.tu.backend.model.constant.ValidationConstraints;
import pl.kielce.tu.backend.model.dto.UserDto;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserValidator validator;

    private int minNick;
    private int maxNick;
    private int minPass;
    private int maxPass;

    @BeforeEach
    void setUp() {
        minNick = ValidationConstraints.MIN_NICKNAME_LENGTH.getValue();
        maxNick = ValidationConstraints.MAX_NICKNAME_LENGTH.getValue();
        minPass = ValidationConstraints.MIN_PASSWORD_LENGTH.getValue();
        maxPass = ValidationConstraints.MAX_PASSWORD_LENGTH.getValue();
    }

    private String repeatChar(char c, int times) {
        return IntStream.range(0, times).mapToObj(i -> String.valueOf(c)).collect(Collectors.joining());
    }

    @Test
    void validate_nullUserDto_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validate(null));
    }

    @Test
    void validate_validUserDto_doesNotThrow() {
        UserDto dto = org.mockito.Mockito.mock(UserDto.class);
        when(dto.getNickname()).thenReturn("valid_nick");
        when(dto.getPassword()).thenReturn(repeatChar('a', Math.max(minPass, 8)));

        assertDoesNotThrow(() -> validator.validate(dto));
    }

    @Test
    void validate_nicknameEmpty_throwsValidationException() {
        UserDto dto = org.mockito.Mockito.mock(UserDto.class);
        when(dto.getNickname()).thenReturn("   ");

        assertThrows(ValidationException.class, () -> validator.validate(dto));
    }

    @Test
    void validate_nicknameTooShort_throwsValidationException() {
        UserDto dto = org.mockito.Mockito.mock(UserDto.class);
        when(dto.getNickname()).thenReturn(repeatChar('a', Math.max(0, minNick - 1)));

        assertThrows(ValidationException.class, () -> validator.validate(dto));
    }

    @Test
    void validate_nicknameTooLong_throwsValidationException() {
        UserDto dto = org.mockito.Mockito.mock(UserDto.class);
        when(dto.getNickname()).thenReturn(repeatChar('a', maxNick + 1));

        assertThrows(ValidationException.class, () -> validator.validate(dto));
    }

    @Test
    void validate_nicknameInvalidPattern_throwsValidationException() {
        UserDto dto = org.mockito.Mockito.mock(UserDto.class);
        when(dto.getNickname()).thenReturn("bad!nick");

        assertThrows(ValidationException.class, () -> validator.validate(dto));
    }

    @Test
    void validate_passwordEmpty_throwsValidationException() {
        UserDto dto = org.mockito.Mockito.mock(UserDto.class);
        when(dto.getNickname()).thenReturn("validnick");
        when(dto.getPassword()).thenReturn(" ");

        assertThrows(ValidationException.class, () -> validator.validate(dto));
    }

    @Test
    void validate_passwordTooShort_throwsValidationException() {
        UserDto dto = org.mockito.Mockito.mock(UserDto.class);
        when(dto.getNickname()).thenReturn("validnick");
        when(dto.getPassword()).thenReturn(repeatChar('a', Math.max(0, minPass - 1)));

        assertThrows(ValidationException.class, () -> validator.validate(dto));
    }

    @Test
    void validate_passwordTooLong_throwsValidationException() {
        UserDto dto = org.mockito.Mockito.mock(UserDto.class);
        when(dto.getNickname()).thenReturn("validnick");
        when(dto.getPassword()).thenReturn(repeatChar('a', maxPass + 1));

        assertThrows(ValidationException.class, () -> validator.validate(dto));
    }

    @Test
    void validateUserExists_nullId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateUserExists(null));
    }

    @Test
    void validateUserExists_nonExistingUser_throwsValidationException() {
        Long id = 42L;
        when(userRepository.findById(eq(id))).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> validator.validateUserExists(id));
    }

    @Test
    void validateUserExists_existingUser_doesNotThrow() {
        Long id = 1L;
        User user = org.mockito.Mockito.mock(User.class);
        when(userRepository.findById(eq(id))).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> validator.validateUserExists(id));
    }

    @Test
    void validateUserIsAdmin_nonAdmin_throwsValidationException() {
        Long id = 2L;
        User user = org.mockito.Mockito.mock(User.class);
        when(userRepository.findById(eq(id))).thenReturn(Optional.of(user));
        when(user.getRank()).thenReturn(RankType.USER);

        assertThrows(ValidationException.class, () -> validator.validateUserIsAdmin(id));
    }

    @Test
    void validateUserIsAdmin_admin_doesNotThrow() {
        Long id = 3L;
        User user = org.mockito.Mockito.mock(User.class);
        when(userRepository.findById(eq(id))).thenReturn(Optional.of(user));
        when(user.getRank()).thenReturn(RankType.ADMIN);

        assertDoesNotThrow(() -> validator.validateUserIsAdmin(id));
    }
}
