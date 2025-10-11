package pl.kielce.tu.backend.filter.strategy;

import java.io.IOException;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.filter.util.ResponseHelper;
import pl.kielce.tu.backend.model.constant.TokenValidationNames;
import pl.kielce.tu.backend.service.auth.CookieService;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class TokenPresenceValidationStrategy implements ValidationStrategy<TokenPresenceInput> {

    private final CookieService cookieService;
    private final ResponseHelper responseHelper;
    private final UserContextLogger userContextLogger;

    @Override
    public ValidationResult validate(TokenPresenceInput input, HttpServletResponse response,
            String requestPath) throws IOException {
        String token = cookieService.getTokenFromCookie(input.request(), input.tokenType());
        if (token == null || token.isEmpty()) {
            userContextLogger.logUserOperation("TOKEN_NOT_FOUND",
                    "No " + input.tokenType() + " found in request to " + requestPath);
            responseHelper.sendUnauthorized(response, "Token not found");
            return new ValidationResult(false, null);
        }
        return new ValidationResult(true, token);
    }

    @Override
    public TokenValidationNames getName() {
        return TokenValidationNames.PRESENCE;
    }

}
