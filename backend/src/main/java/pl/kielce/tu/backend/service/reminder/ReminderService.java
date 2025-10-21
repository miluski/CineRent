package pl.kielce.tu.backend.service.reminder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.extractor.ClaimsExtractor;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.dto.DvdReminderRequestDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.DvdReminder;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.DvdReminderRepository;
import pl.kielce.tu.backend.repository.DvdRepository;
import pl.kielce.tu.backend.repository.UserRepository;
import pl.kielce.tu.backend.service.auth.CookieService;
import pl.kielce.tu.backend.util.UserContextLogger;

@Service
@RequiredArgsConstructor
public class ReminderService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final CookieService cookieService;
    private final ClaimsExtractor claimsExtractor;
    private final UserRepository userRepository;
    private final DvdRepository dvdRepository;
    private final DvdReminderRepository dvdReminderRepository;
    private final UserContextLogger userContextLogger;

    public ResponseEntity<Void> handleCreateReminder(HttpServletRequest request,
            DvdReminderRequestDto reminderRequest) {
        try {
            Long userId = extractUserIdFromRequest(request);
            User user = findUserById(userId);
            validateUserIsVerified(user);
            Dvd dvd = findDvdById(reminderRequest.getDvdId());
            preventDuplicateReminder(userId, dvd.getId());
            createAndSaveReminder(user, dvd);
            logSuccessfulCreation(dvd);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (ValidationException e) {
            return handleValidationError(e);
        } catch (EntityNotFoundException e) {
            return handleNotFoundError(e);
        } catch (Exception e) {
            return handleUnexpectedError(e);
        }
    }

    private void logSuccessfulCreation(Dvd dvd) {
        userContextLogger.logUserOperation("CREATE_REMINDER",
                String.format("DVD ID: %d, DVD Title: %s", dvd.getId(), dvd.getTitle()));
    }

    private ResponseEntity<Void> handleValidationError(ValidationException e) {
        userContextLogger.logValidationOperation("REMINDER_CREATION", "FAILED", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    private ResponseEntity<Void> handleNotFoundError(EntityNotFoundException e) {
        userContextLogger.logUserOperation("CREATE_REMINDER_FAILED", "Resource not found: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    private ResponseEntity<Void> handleUnexpectedError(Exception e) {
        userContextLogger.logUserOperation("CREATE_REMINDER_ERROR", "Unexpected error: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN);
        if (token == null) {
            throw new IllegalArgumentException("Missing authentication token");
        }
        return claimsExtractor.extractUserId(token, jwtSecret);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private void validateUserIsVerified(User user) throws ValidationException {
        if (!Boolean.TRUE.equals(user.getIsVerified())) {
            throw new ValidationException("User email must be verified to create reminders");
        }
    }

    private Dvd findDvdById(Long dvdId) {
        return dvdRepository.findById(dvdId)
                .orElseThrow(() -> new EntityNotFoundException("DVD not found"));
    }

    private void preventDuplicateReminder(Long userId, Long dvdId) throws ValidationException {
        if (dvdReminderRepository.existsByUserIdAndDvdId(userId, dvdId)) {
            throw new ValidationException("Reminder already exists for this DVD");
        }
    }

    private void createAndSaveReminder(User user, Dvd dvd) {
        DvdReminder reminder = DvdReminder.builder()
                .user(user)
                .dvd(dvd)
                .build();
        dvdReminderRepository.save(reminder);
    }

}
