package pl.kielce.tu.backend.filter.strategy;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kielce.tu.backend.extractor.ClaimsExtractor;
import pl.kielce.tu.backend.filter.util.ResponseHelper;
import pl.kielce.tu.backend.model.constant.TokenValidationNames;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenExtractionValidationStrategy implements ValidationStrategy<String> {

    private final ResponseHelper responseHelper;
    private final ClaimsExtractor claimsExtractor;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public ValidationResult validate(String token, HttpServletResponse response, String requestPath)
            throws IOException {
        try {
            Long userId = claimsExtractor.extractUserId(token, jwtSecret);
            return new ValidationResult(true, userId);
        } catch (Exception e) {
            log.debug("Failed to extract user ID from token for {}", requestPath);
            responseHelper.sendUnauthorized(response, "Invalid token");
            return new ValidationResult(false, null);
        }
    }

    @Override
    public TokenValidationNames getName() {
        return TokenValidationNames.EXTRACTION;
    }

}
