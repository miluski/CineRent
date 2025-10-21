package pl.kielce.tu.backend.service.reminder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.HttpServletRequest;
import pl.kielce.tu.backend.extractor.ClaimsExtractor;
import pl.kielce.tu.backend.model.dto.DvdReminderRequestDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.DvdReminder;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.DvdReminderRepository;
import pl.kielce.tu.backend.repository.DvdRepository;
import pl.kielce.tu.backend.repository.UserRepository;
import pl.kielce.tu.backend.service.auth.CookieService;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private CookieService cookieService;

    @Mock
    private ClaimsExtractor claimsExtractor;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DvdRepository dvdRepository;

    @Mock
    private DvdReminderRepository dvdReminderRepository;

    @Mock
    private UserContextLogger userContextLogger;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private ReminderService reminderService;

    private User verifiedUser;
    private User unverifiedUser;
    private Dvd dvd;
    private DvdReminderRequestDto reminderRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reminderService, "jwtSecret", "test-secret");

        verifiedUser = User.builder()
                .id(1L)
                .email("verified@example.com")
                .nickname("verifieduser")
                .isVerified(true)
                .build();

        unverifiedUser = User.builder()
                .id(2L)
                .email("unverified@example.com")
                .nickname("unverifieduser")
                .isVerified(false)
                .build();

        dvd = Dvd.builder()
                .id(42L)
                .title("The Matrix")
                .avalaible(false)
                .copiesAvalaible(0)
                .build();

        reminderRequest = DvdReminderRequestDto.builder()
                .dvdId(42L)
                .build();
    }

    @Test
    void handleCreateReminder_Success_VerifiedUser() {
        when(cookieService.getTokenFromCookie(eq(httpServletRequest), any())).thenReturn("valid-token");
        when(claimsExtractor.extractUserId(anyString(), anyString())).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(verifiedUser));
        when(dvdRepository.findById(42L)).thenReturn(Optional.of(dvd));
        when(dvdReminderRepository.existsByUserIdAndDvdId(1L, 42L)).thenReturn(false);
        when(dvdReminderRepository.save(any(DvdReminder.class))).thenReturn(new DvdReminder());

        ResponseEntity<Void> response = reminderService.handleCreateReminder(httpServletRequest, reminderRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(dvdReminderRepository).save(any(DvdReminder.class));
        verify(userContextLogger).logUserOperation(eq("CREATE_REMINDER"), anyString());
    }

    @Test
    void handleCreateReminder_Forbidden_UnverifiedUser() {
        when(cookieService.getTokenFromCookie(eq(httpServletRequest), any())).thenReturn("valid-token");
        when(claimsExtractor.extractUserId(anyString(), anyString())).thenReturn(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(unverifiedUser));

        ResponseEntity<Void> response = reminderService.handleCreateReminder(httpServletRequest, reminderRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(dvdReminderRepository, never()).save(any());
        verify(userContextLogger).logValidationOperation(eq("REMINDER_CREATION"), eq("FAILED"), anyString());
    }

    @Test
    void handleCreateReminder_Forbidden_DuplicateReminder() {
        when(cookieService.getTokenFromCookie(eq(httpServletRequest), any())).thenReturn("valid-token");
        when(claimsExtractor.extractUserId(anyString(), anyString())).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(verifiedUser));
        when(dvdRepository.findById(42L)).thenReturn(Optional.of(dvd));
        when(dvdReminderRepository.existsByUserIdAndDvdId(1L, 42L)).thenReturn(true);

        ResponseEntity<Void> response = reminderService.handleCreateReminder(httpServletRequest, reminderRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(dvdReminderRepository, never()).save(any());
    }

    @Test
    void handleCreateReminder_NotFound_UserNotFound() {
        when(cookieService.getTokenFromCookie(eq(httpServletRequest), any())).thenReturn("valid-token");
        when(claimsExtractor.extractUserId(anyString(), anyString())).thenReturn(999L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = reminderService.handleCreateReminder(httpServletRequest, reminderRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(dvdReminderRepository, never()).save(any());
    }

    @Test
    void handleCreateReminder_NotFound_DvdNotFound() {
        when(cookieService.getTokenFromCookie(eq(httpServletRequest), any())).thenReturn("valid-token");
        when(claimsExtractor.extractUserId(anyString(), anyString())).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(verifiedUser));
        when(dvdRepository.findById(42L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = reminderService.handleCreateReminder(httpServletRequest, reminderRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(dvdReminderRepository, never()).save(any());
    }

    @Test
    void handleCreateReminder_InternalServerError_MissingToken() {
        when(cookieService.getTokenFromCookie(eq(httpServletRequest), any())).thenReturn(null);

        ResponseEntity<Void> response = reminderService.handleCreateReminder(httpServletRequest, reminderRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(dvdReminderRepository, never()).save(any());
    }

}
