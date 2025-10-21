package pl.kielce.tu.backend.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.kielce.tu.backend.exception.EmailSendingException;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.mapper.UserMapper;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.model.dto.UserDto;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.UserRepository;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;
import pl.kielce.tu.backend.service.validation.factory.ValidationStrategyFactory;
import pl.kielce.tu.backend.service.verification.VerificationService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private TokenService tokenService;
    @Mock
    private CookieService cookieService;
    @Mock
    private ValidationStrategyFactory validationStrategyFactory;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private VerificationService verificationService;

    @InjectMocks
    private AuthService authService;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @SuppressWarnings("rawtypes")
    private FieldValidationStrategy nicknameStrategy;
    @SuppressWarnings("rawtypes")
    private FieldValidationStrategy passwordStrategy;
    @SuppressWarnings("rawtypes")
    private FieldValidationStrategy ageStrategy;
    @SuppressWarnings("rawtypes")
    private FieldValidationStrategy genreStrategy;

    private UserDto userDto;
    private User user;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws ValidationException {
        userDto = UserDto.builder()
                .nickname("nick")
                .email("test@example.com")
                .password("rawPass")
                .build();

        user = new User();
        user.setId(1L);
        user.setNickname("nick");
        user.setEmail("test@example.com");
        user.setPassword("encodedPass");
        user.setIsVerified(true);

        nicknameStrategy = mock(FieldValidationStrategy.class);
        passwordStrategy = mock(FieldValidationStrategy.class);
        ageStrategy = mock(FieldValidationStrategy.class);
        genreStrategy = mock(FieldValidationStrategy.class);

        lenient().when(validationStrategyFactory.getStrategy(ValidationStrategyType.NICKNAME))
                .thenReturn(nicknameStrategy);
        lenient().when(validationStrategyFactory.getStrategy(ValidationStrategyType.PASSWORD))
                .thenReturn(passwordStrategy);
        lenient().when(validationStrategyFactory.getStrategy(ValidationStrategyType.AGE)).thenReturn(ageStrategy);
        lenient().when(validationStrategyFactory.getStrategy(ValidationStrategyType.GENRE)).thenReturn(genreStrategy);

        lenient().doNothing().when(nicknameStrategy).validate(any());
        lenient().doNothing().when(passwordStrategy).validate(any());
        lenient().doNothing().when(ageStrategy).validate(any());
        lenient().doNothing().when(genreStrategy).validate(any());
    }

    @Test
    void handleLogin_success_returnsOkAndSetsCookies() throws ValidationException {
        when(userRepository.findByNickname("nick")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPass", "encodedPass")).thenReturn(true);
        when(tokenService.generateToken(eq(user), eq(CookieNames.ACCESS_TOKEN)))
                .thenReturn("accessToken");
        when(tokenService.generateToken(eq(user), eq(CookieNames.REFRESH_TOKEN)))
                .thenReturn("refreshToken");

        ResponseEntity<Void> responseEntity = authService.handleLogin(userDto, response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(cookieService).setAccessTokenCookie(response, "accessToken");
        verify(cookieService).setRefreshTokenCookie(response, "refreshToken");
    }

    @SuppressWarnings("unchecked")
    @Test
    void handleLogin_validationFails_returnsUnprocessableEntity() throws ValidationException {
        doThrow(new ValidationException("bad")).when(nicknameStrategy).validate(any());

        ResponseEntity<Void> responseEntity = authService.handleLogin(userDto, response);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, responseEntity.getStatusCode());
        verifyNoInteractions(tokenService);
    }

    @Test
    void handleLogin_badCredentials_returnsUnauthorized() throws ValidationException {
        when(userRepository.findByNickname("nick")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPass", "encodedPass")).thenReturn(false);

        ResponseEntity<Void> responseEntity = authService.handleLogin(userDto, response);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    void handleLogout_success_returnsOk_andDeletesCookiesAndBlacklists() {
        doNothing().when(tokenService).blacklistRequestTokens(request);

        ResponseEntity<Void> responseEntity = authService.handleLogout(request, response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(tokenService).blacklistRequestTokens(request);
        verify(cookieService).deleteTokenCookie(response, CookieNames.ACCESS_TOKEN);
        verify(cookieService).deleteTokenCookie(response, CookieNames.REFRESH_TOKEN);
    }

    @Test
    void handleLogout_tokenServiceThrows_returnsInternalServerError() {
        doThrow(new RuntimeException("boom")).when(tokenService).blacklistRequestTokens(request);

        ResponseEntity<Void> responseEntity = authService.handleLogout(request, response);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    void handleRegister_success_returnsCreated_andSavesUserAndSendsEmail() throws ValidationException {
        when(userMapper.toUser(userDto)).thenReturn(user);
        when(passwordEncoder.encode("rawPass")).thenReturn("encodedPass");
        when(userRepository.save(user)).thenReturn(user);
        doNothing().when(verificationService).generateAndSendVerificationCode("test@example.com");

        ResponseEntity<Void> responseEntity = authService.handleRegister(userDto);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        verify(passwordEncoder).encode("rawPass");
        verify(userRepository).save(user);
        verify(verificationService).generateAndSendVerificationCode("test@example.com");
    }

    @SuppressWarnings("unchecked")
    @Test
    void handleRegister_validationFails_returnsUnprocessableEntity() throws ValidationException {
        doThrow(new ValidationException("bad")).when(nicknameStrategy).validate(any());

        ResponseEntity<Void> responseEntity = authService.handleRegister(userDto);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, responseEntity.getStatusCode());
        verifyNoInteractions(userRepository);
    }

    @Test
    void handleRegister_repositoryThrows_returnsInternalServerError() throws ValidationException {
        when(userMapper.toUser(userDto)).thenReturn(user);
        when(passwordEncoder.encode("rawPass")).thenReturn("encodedPass");
        doThrow(new RuntimeException("db")).when(userRepository).save(user);

        ResponseEntity<Void> responseEntity = authService.handleRegister(userDto);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    void handleRegister_emailSendingFails_returnsCreated() throws ValidationException {
        when(userMapper.toUser(userDto)).thenReturn(user);
        when(passwordEncoder.encode("rawPass")).thenReturn("encodedPass");
        when(userRepository.save(user)).thenReturn(user);
        doThrow(new EmailSendingException("Email failed")).when(verificationService)
                .generateAndSendVerificationCode("test@example.com");

        ResponseEntity<Void> responseEntity = authService.handleRegister(userDto);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        verify(userRepository).save(user);
    }

    @Test
    void handleRefreshTokens_success_returnsOk_andGeneratesNewTokens() {
        when(cookieService.getTokenFromCookie(request, CookieNames.REFRESH_TOKEN)).thenReturn("refreshTokenValue");
        when(tokenService.extractUserIdFromToken("refreshTokenValue")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(tokenService).blacklistRequestTokens(request);
        when(tokenService.generateToken(eq(user), eq(CookieNames.ACCESS_TOKEN))).thenReturn("newAccess");
        when(tokenService.generateToken(eq(user), eq(CookieNames.REFRESH_TOKEN)))
                .thenReturn("newRefresh");

        ResponseEntity<Void> responseEntity = authService.handleRefreshTokens(request, response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(cookieService).setAccessTokenCookie(response, "newAccess");
        verify(cookieService).setRefreshTokenCookie(response, "newRefresh");
        verify(tokenService).blacklistRequestTokens(request);
    }

    @Test
    void handleRefreshTokens_invalidToken_returnsUnauthorized() {
        when(cookieService.getTokenFromCookie(request, CookieNames.REFRESH_TOKEN)).thenReturn("badToken");
        when(tokenService.extractUserIdFromToken("badToken")).thenThrow(new RuntimeException("invalid"));

        ResponseEntity<Void> responseEntity = authService.handleRefreshTokens(request, response);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }
}
