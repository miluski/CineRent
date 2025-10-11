package pl.kielce.tu.backend.util;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import pl.kielce.tu.backend.model.constant.LoggingConstants;

@Slf4j
@Component
public class UserContextLogger {

    public Optional<Long> getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Long userId) {
                return Optional.of(userId);
            }
        } catch (Exception e) {
            log.debug("Failed to extract user ID from SecurityContext: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public String getUserIdentifierForLogging() {
        return getCurrentUserId()
                .map(id -> LoggingConstants.USER_PREFIX.getValue() + id)
                .orElse(LoggingConstants.ANONYMOUS_USER.getValue());
    }

    public void logUserOperation(String operation, String details) {
        String separator = LoggingConstants.LOG_SEPARATOR.getValue();
        logWithUserContext("Operation: {}" + separator + "User: {}" + separator + "Details: {}",
                operation, details);
    }

    public void logValidationOperation(String validationType, String result, String details) {
        String separator = LoggingConstants.LOG_SEPARATOR.getValue();
        logWithUserContext(
                "Validation: {}" + separator + "User: {}" + separator + "Result: {}" + separator + "Details: {}",
                validationType, result, details);
    }

    public void logEndpointAccess(String method, String endpoint, String status) {
        String separator = LoggingConstants.LOG_SEPARATOR.getValue();
        logWithUserContext("Endpoint Access: {}" + separator + "User: {}" + separator + "Status: {}",
                method + " " + endpoint, status);
    }

    private void logWithUserContext(String pattern, Object... args) {
        String userIdentifier = getUserIdentifierForLogging();
        Object[] allArgs = new Object[args.length + 1];
        if (args.length >= 2) {
            allArgs[0] = args[0];
            allArgs[1] = userIdentifier;
            System.arraycopy(args, 1, allArgs, 2, args.length - 1);
        } else if (args.length == 1) {
            allArgs[0] = args[0];
            allArgs[1] = userIdentifier;
        } else {
            allArgs[0] = userIdentifier;
        }
        log.info(pattern, allArgs);
    }

}
