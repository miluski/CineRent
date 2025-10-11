package pl.kielce.tu.backend.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.HttpServletRequest;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.extractor.ClaimsExtractor;
import pl.kielce.tu.backend.mapper.UserMapper;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.model.dto.UserDto;
import pl.kielce.tu.backend.model.entity.Genre;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.UserRepository;
import pl.kielce.tu.backend.service.auth.CookieService;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;
import pl.kielce.tu.backend.service.validation.factory.ValidationStrategyFactory;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ValidationStrategyFactory validationStrategyFactory;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ClaimsExtractor claimsExtractor;

    @Mock
    private CookieService cookieService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private UserService userService;

    private static final String JWT_SECRET = "test-secret";
    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "jwtSecret", JWT_SECRET);
    }

    @Test
    void handleGetUser_returnsUserDto_whenUserExists() {
        User user = createTestUser();
        UserDto expectedDto = createTestUserDto();

        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(VALID_TOKEN);
        when(claimsExtractor.extractUserId(VALID_TOKEN, JWT_SECRET)).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(expectedDto);

        ResponseEntity<UserDto> response = userService.handleGetUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserDto body = response.getBody();
        assertNotNull(body);
        assertEquals("FilmLover99", body.getNickname());
        assertEquals(24, body.getAge());
        verify(cookieService).getTokenFromCookie(request, CookieNames.ACCESS_TOKEN);
        verify(claimsExtractor).extractUserId(VALID_TOKEN, JWT_SECRET);
        verify(userRepository).findById(USER_ID);
        verify(userMapper).toDto(user);
    }

    @Test
    void handleGetUser_returnsNotFound_whenUserDoesNotExist() {
        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(VALID_TOKEN);
        when(claimsExtractor.extractUserId(VALID_TOKEN, JWT_SECRET)).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        ResponseEntity<UserDto> response = userService.handleGetUser(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void handleGetUser_returnsInternalServerError_whenTokenIsNull() {
        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(null);

        ResponseEntity<UserDto> response = userService.handleGetUser(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(claimsExtractor, never()).extractUserId(anyString(), anyString());
    }

    @Test
    void handleEditUser_updatesUser_whenAllFieldsProvided() throws ValidationException {
        User user = createTestUser();
        UserDto updateDto = createUpdateDto();

        setupMocksForEdit(user);
        setupValidationMocks();

        when(passwordEncoder.encode("NewPassword123!")).thenReturn("encoded-new-password");

        User userWithGenres = User.builder()
                .preferredGenres(Arrays.asList(
                        Genre.builder().id(1L).name("Komedia").build(),
                        Genre.builder().id(3L).name("Dramat").build()))
                .build();
        when(userMapper.toUser(any(UserDto.class))).thenReturn(userWithGenres);

        ResponseEntity<Void> response = userService.handleEditUser(request, updateDto);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(userRepository).save(user);
        assertEquals("NewNickname", user.getNickname());
        assertEquals("encoded-new-password", user.getPassword());
        assertEquals(30, user.getAge());
    }

    @Test
    void handleEditUser_updatesOnlyNickname_whenOnlyNicknameProvided() throws ValidationException {
        User user = createTestUser();
        String originalPassword = user.getPassword();
        Integer originalAge = user.getAge();

        UserDto updateDto = UserDto.builder()
                .nickname("UpdatedNickname")
                .build();

        setupMocksForEdit(user);
        setupValidationMock(ValidationStrategyType.NICKNAME);

        ResponseEntity<Void> response = userService.handleEditUser(request, updateDto);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(userRepository).save(user);
        assertEquals("UpdatedNickname", user.getNickname());
        assertEquals(originalPassword, user.getPassword());
        assertEquals(originalAge, user.getAge());
    }

    @Test
    void handleEditUser_updatesOnlyPassword_whenOnlyPasswordProvided() throws ValidationException {
        User user = createTestUser();
        String originalNickname = user.getNickname();

        UserDto updateDto = UserDto.builder()
                .password("NewSecurePass!")
                .build();

        setupMocksForEdit(user);
        setupValidationMock(ValidationStrategyType.PASSWORD);
        when(passwordEncoder.encode("NewSecurePass!")).thenReturn("encoded-password");

        ResponseEntity<Void> response = userService.handleEditUser(request, updateDto);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(userRepository).save(user);
        assertEquals(originalNickname, user.getNickname());
        assertEquals("encoded-password", user.getPassword());
    }

    @Test
    void handleEditUser_returnsUnprocessableEntity_whenNoFieldsProvided() {
        UserDto emptyDto = UserDto.builder().build();

        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(VALID_TOKEN);
        when(claimsExtractor.extractUserId(VALID_TOKEN, JWT_SECRET)).thenReturn(USER_ID);

        ResponseEntity<Void> response = userService.handleEditUser(request, emptyDto);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void handleEditUser_returnsNotFound_whenUserDoesNotExist() {
        UserDto updateDto = UserDto.builder().nickname("Test").build();

        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(VALID_TOKEN);
        when(claimsExtractor.extractUserId(VALID_TOKEN, JWT_SECRET)).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = userService.handleEditUser(request, updateDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void handleEditUser_returnsUnprocessableEntity_whenValidationFails() throws ValidationException {
        User user = createTestUser();
        UserDto updateDto = UserDto.builder().nickname("a").build();

        setupMocksForEdit(user);

        FieldValidationStrategy strategy = mock(FieldValidationStrategy.class);
        when(validationStrategyFactory.getStrategy(ValidationStrategyType.NICKNAME)).thenReturn(strategy);
        doThrow(new ValidationException("Nickname too short")).when(strategy).validate("a");

        ResponseEntity<Void> response = userService.handleEditUser(request, updateDto);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    private User createTestUser() {
        return User.builder()
                .id(USER_ID)
                .nickname("FilmLover99")
                .password("encoded-password")
                .age(24)
                .preferredGenres(Arrays.asList(
                        Genre.builder().id(1L).name("Komedia").build(),
                        Genre.builder().id(2L).name("Sci-Fi").build()))
                .build();
    }

    private UserDto createTestUserDto() {
        return UserDto.builder()
                .nickname("FilmLover99")
                .age(24)
                .preferredGenres(Arrays.asList("Komedia", "Sci-Fi"))
                .preferredGenresIdentifiers(Arrays.asList(1L, 2L))
                .build();
    }

    private UserDto createUpdateDto() {
        return UserDto.builder()
                .nickname("NewNickname")
                .password("NewPassword123!")
                .age(30)
                .preferredGenresIdentifiers(Arrays.asList(1L, 3L))
                .build();
    }

    private void setupMocksForEdit(User user) {
        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(VALID_TOKEN);
        when(claimsExtractor.extractUserId(VALID_TOKEN, JWT_SECRET)).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        lenient().when(userRepository.save(user)).thenReturn(user);
    }

    private void setupValidationMocks() throws ValidationException {
        setupValidationMock(ValidationStrategyType.NICKNAME);
        setupValidationMock(ValidationStrategyType.PASSWORD);
        setupValidationMock(ValidationStrategyType.AGE);
        setupValidationMock(ValidationStrategyType.GENRE);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void setupValidationMock(ValidationStrategyType type) throws ValidationException {
        FieldValidationStrategy strategy = mock(FieldValidationStrategy.class);
        when(validationStrategyFactory.getStrategy(type)).thenReturn(strategy);
        lenient().doNothing().when(strategy).validate(any());
    }

}
