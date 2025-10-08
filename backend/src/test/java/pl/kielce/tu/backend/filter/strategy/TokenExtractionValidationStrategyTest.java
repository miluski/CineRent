package pl.kielce.tu.backend.filter.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletResponse;
import pl.kielce.tu.backend.extractor.ClaimsExtractor;
import pl.kielce.tu.backend.filter.util.ResponseHelper;
import pl.kielce.tu.backend.model.constant.TokenValidationNames;

class TokenExtractionValidationStrategyTest {

    private ResponseHelper responseHelper;
    private ClaimsExtractor claimsExtractor;
    private TokenExtractionValidationStrategy strategy;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() throws Exception {
        responseHelper = mock(ResponseHelper.class);
        claimsExtractor = mock(ClaimsExtractor.class);
        response = mock(HttpServletResponse.class);

        strategy = new TokenExtractionValidationStrategy(responseHelper, claimsExtractor);

        Field jwtField = TokenExtractionValidationStrategy.class.getDeclaredField("jwtSecret");
        jwtField.setAccessible(true);
        jwtField.set(strategy, "test-secret");
    }

    @Test
    void validate_whenExtractionSucceeds_returnsValidResultAndDoesNotCallSendUnauthorized() throws IOException {
        String token = "valid-token";
        when(claimsExtractor.extractUserId(token, "test-secret")).thenReturn(42L);

        ValidationResult result = strategy.validate(token, response, "/some/path");

        assertTrue(result.isSuccess());
        assertEquals(42L, result.getData());
        verifyNoInteractions(responseHelper);
    }

    @Test
    void validate_whenExtractionFails_sendsUnauthorizedAndReturnsInvalidResult() throws IOException {
        String token = "invalid-token";
        when(claimsExtractor.extractUserId(token, "test-secret")).thenThrow(new RuntimeException("bad token"));

        ValidationResult result = strategy.validate(token, response, "/other/path");

        assertFalse(result.isSuccess());
        assertNull(result.getData());
        verify(responseHelper).sendUnauthorized(response, "Invalid token");
    }

    @Test
    void getName_returnsExtraction() {
        assertEquals(TokenValidationNames.EXTRACTION, strategy.getName());
    }
}
