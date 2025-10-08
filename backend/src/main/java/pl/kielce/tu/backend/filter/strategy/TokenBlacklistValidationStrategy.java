package pl.kielce.tu.backend.filter.strategy;

import java.io.IOException;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kielce.tu.backend.filter.util.ResponseHelper;
import pl.kielce.tu.backend.model.constant.TokenValidationNames;
import pl.kielce.tu.backend.service.auth.TokenService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenBlacklistValidationStrategy implements ValidationStrategy<String> {

    private final TokenService tokenService;
    private final ResponseHelper responseHelper;

    @Override
    public ValidationResult validate(String token, HttpServletResponse response, String requestPath)
            throws IOException {
        if (tokenService.isTokenBlacklisted(token)) {
            log.debug("Blacklisted token used for {}", requestPath);
            responseHelper.sendUnauthorized(response, "Token is blacklisted");
            return new ValidationResult(false, null);
        }
        return new ValidationResult(true, token);
    }

    @Override
    public TokenValidationNames getName() {
        return TokenValidationNames.BLACKLIST;
    }

}
