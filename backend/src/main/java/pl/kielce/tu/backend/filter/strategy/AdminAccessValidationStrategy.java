package pl.kielce.tu.backend.filter.strategy;

import java.io.IOException;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.filter.util.ResponseHelper;
import pl.kielce.tu.backend.model.constant.AdminEndpoints;
import pl.kielce.tu.backend.model.constant.RankType;
import pl.kielce.tu.backend.model.constant.TokenValidationNames;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class AdminAccessValidationStrategy implements ValidationStrategy<User> {

    private final ResponseHelper responseHelper;
    private final UserContextLogger userContextLogger;

    @Override
    public ValidationResult validate(User user, HttpServletResponse response, String requestPath) throws IOException {
        if (AdminEndpoints.isMember(requestPath) && user.getRank() != RankType.ADMIN) {
            userContextLogger.logUserOperation("ADMIN_ACCESS_DENIED",
                    "User " + user.getId() + " attempted to access admin endpoint " + requestPath);
            responseHelper.sendForbidden(response, "Insufficient permissions");
            return new ValidationResult(false, null);
        }

        return new ValidationResult(true, user);
    }

    @Override
    public TokenValidationNames getName() {
        return TokenValidationNames.ADMIN_ACCESS;
    }

}
