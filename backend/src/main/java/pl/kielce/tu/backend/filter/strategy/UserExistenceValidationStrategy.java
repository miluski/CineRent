package pl.kielce.tu.backend.filter.strategy;

import java.io.IOException;
import java.util.Optional;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.filter.util.ResponseHelper;
import pl.kielce.tu.backend.model.constant.TokenValidationNames;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.UserRepository;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class UserExistenceValidationStrategy implements ValidationStrategy<Long> {

    private final UserRepository userRepository;
    private final ResponseHelper responseHelper;
    private final UserContextLogger userContextLogger;

    @Override
    public ValidationResult validate(Long userId, HttpServletResponse response, String requestPath) throws IOException {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            userContextLogger.logUserOperation("USER_NOT_FOUND", "User with ID " + userId + " not found");
            responseHelper.sendUnauthorized(response, "User not found");
            return new ValidationResult(false, null);
        }

        return new ValidationResult(true, userOptional.get());
    }

    @Override
    public TokenValidationNames getName() {
        return TokenValidationNames.USER_EXISTENCE;
    }

}
