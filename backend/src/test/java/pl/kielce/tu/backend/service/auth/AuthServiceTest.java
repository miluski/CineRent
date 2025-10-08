package pl.kielce.tu.backend.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.mapper.UserMapper;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.dto.UserDto;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.UserRepository;
import pl.kielce.tu.backend.service.validation.UserValidator;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private TokenService tokenService;
    @Mock
    private CookieService cookieService;
    @Mock
    private UserValidator userValidator;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .nickname("nick")
                .password("rawPass")
                .isRemembered(false)
                .build();

        user = new User();
        user.setId(1L);
        user.setNickname("nick");
        user.setPassword("encodedPass");
    }

    @Test
    void handleLogin_success_returnsOkAndSetsCookies() throws ValidationException {
        doNothing().when(userValidator).validate(userDto);
        when(userRepository.findByNickname("nick")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPass", "encodedPass")).thenReturn(true);
        when(tokenService.generateToken(eq(user), anyBoolean(), eq(CookieNames.ACCESS_TOKEN)))
                .thenReturn("accessToken");
        when(tokenService.generateToken(eq(user), anyBoolean(), eq(CookieNames.REFRESH_TOKEN)))
                .thenReturn("refreshToken");

        ResponseEntity<Void> responseEntity = authService.handleLogin(userDto, response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(cookieService).setAccessTokenCookie(response, "accessToken");
        verify(cookieService).setRefreshTokenCookie(response, "refreshToken", userDto.isRemembered());
    }

    @Test
    void handleLogin_validationFails_returnsUnprocessableEntity() throws ValidationException {
        doThrow(new ValidationException("bad")).when(userValidator).validate(userDto);

        ResponseEntity<Void> responseEntity = authService.handleLogin(userDto, response);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, responseEntity.getStatusCode());
        verifyNoInteractions(tokenService);
    }

    @Test
    void handleLogin_badCredentials_returnsUnauthorized() throws ValidationException {
        doNothing().when(userValidator).validate(userDto);
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
    void handleRegister_success_returnsCreated_andSavesUser() throws ValidationException {
        doNothing().when(userValidator).validate(userDto);
        when(userMapper.toUser(userDto)).thenReturn(user);
        when(passwordEncoder.encode("rawPass")).thenReturn("encodedPass");
        when(userRepository.save(user)).thenReturn(user);

        ResponseEntity<Void> responseEntity = authService.handleRegister(userDto);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        verify(passwordEncoder).encode("rawPass");
        verify(userRepository).save(user);
    }

    @Test
    void handleRegister_validationFails_returnsUnprocessableEntity() throws ValidationException {
        doThrow(new ValidationException("bad")).when(userValidator).validate(userDto);

        ResponseEntity<Void> responseEntity = authService.handleRegister(userDto);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, responseEntity.getStatusCode());
        verifyNoInteractions(userRepository);
    }

    @Test
    void handleRegister_repositoryThrows_returnsInternalServerError() throws ValidationException {
        doNothing().when(userValidator).validate(userDto);
        when(userMapper.toUser(userDto)).thenReturn(user);
        when(passwordEncoder.encode("rawPass")).thenReturn("encodedPass");
        doThrow(new RuntimeException("db")).when(userRepository).save(user);

        ResponseEntity<Void> responseEntity = authService.handleRegister(userDto);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    void handleRefreshTokens_success_returnsOk_andGeneratesNewTokens() {
        when(cookieService.getTokenFromCookie(request, CookieNames.REFRESH_TOKEN)).thenReturn("refreshTokenValue");
        when(tokenService.extractUserIdFromToken("refreshTokenValue")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tokenService.isTokenRemembered("refreshTokenValue")).thenReturn(true);
        doNothing().when(tokenService).blacklistRequestTokens(request);
        when(tokenService.generateToken(eq(user), anyBoolean(), eq(CookieNames.ACCESS_TOKEN))).thenReturn("newAccess");
        when(tokenService.generateToken(eq(user), anyBoolean(), eq(CookieNames.REFRESH_TOKEN)))
                .thenReturn("newRefresh");

        ResponseEntity<Void> responseEntity = authService.handleRefreshTokens(request, response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(cookieService).setAccessTokenCookie(response, "newAccess");
        verify(cookieService).setRefreshTokenCookie(response, "newRefresh", true);
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
