package pl.kielce.tu.backend.service.auth;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.EmailSendingException;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.mapper.UserMapper;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.model.dto.UserDto;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.UserRepository;
import pl.kielce.tu.backend.service.validation.factory.ValidationStrategyFactory;
import pl.kielce.tu.backend.service.verification.VerificationService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final TokenService tokenService;
    private final CookieService cookieService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final ValidationStrategyFactory validationFactory;

    public ResponseEntity<Void> handleLogin(UserDto userDto, HttpServletResponse httpServletResponse) {
        try {
            validateForLogin(userDto);
            User user = authenticateUser(userDto);
            generateAndSetNewTokens(httpServletResponse, user);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    public ResponseEntity<Void> handleLogout(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        try {
            tokenService.blacklistRequestTokens(httpServletRequest);
            cookieService.deleteTokenCookie(httpServletResponse, CookieNames.ACCESS_TOKEN);
            cookieService.deleteTokenCookie(httpServletResponse, CookieNames.REFRESH_TOKEN);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<Void> handleRegister(UserDto userDto) {
        try {
            validateForRegistration(userDto);
            User user = register(userDto);
            sendVerificationEmail(user);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (EmailSendingException e) {
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<Void> handleRefreshTokens(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        try {
            String refreshToken = extractRefreshToken(httpServletRequest);
            Long userId = tokenService.extractUserIdFromToken(refreshToken);
            User user = findUserById(userId);
            tokenService.blacklistRequestTokens(httpServletRequest);
            generateAndSetNewTokens(httpServletResponse, user);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    private User register(UserDto userDto) throws ValidationException {
        try {
            User user = userMapper.toUser(userDto);
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("User with this credentials already exists");
        }
    }

    private User authenticateUser(UserDto userDto) {
        User user = findUserByNickname(userDto.getNickname());
        validatePassword(userDto.getPassword(), user.getPassword());
        return user;
    }

    private User findUserByNickname(String nickname) {
        return userRepository.findByNickname(nickname)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new RuntimeException("Invalid credentials");
        }
    }

    private String extractRefreshToken(HttpServletRequest httpServletRequest) {
        return cookieService.getTokenFromCookie(httpServletRequest, CookieNames.REFRESH_TOKEN);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void generateAndSetNewTokens(HttpServletResponse httpServletResponse, User user) {
        String accessToken = tokenService.generateToken(user, CookieNames.ACCESS_TOKEN);
        String refreshToken = tokenService.generateToken(user, CookieNames.REFRESH_TOKEN);
        cookieService.setAccessTokenCookie(httpServletResponse, accessToken);
        cookieService.setRefreshTokenCookie(httpServletResponse, refreshToken);
    }

    private void validateForLogin(UserDto userDto) throws ValidationException {
        if (userDto == null) {
            throw new ValidationException("User data cannot be null");
        }
        validationFactory.getStrategy(ValidationStrategyType.NICKNAME).validate(userDto.getNickname());
        validationFactory.getStrategy(ValidationStrategyType.PASSWORD).validate(userDto.getPassword());
    }

    private void validateForRegistration(UserDto userDto) throws ValidationException {
        validateForLogin(userDto);
        validationFactory.getStrategy(ValidationStrategyType.AGE).validate(userDto.getAge());
        validationFactory.getStrategy(ValidationStrategyType.GENRE).validate(userDto.getPreferredGenresIdentifiers());
    }

    private void sendVerificationEmail(User user) {
        verificationService.generateAndSendVerificationCode(user.getEmail());
    }

}
