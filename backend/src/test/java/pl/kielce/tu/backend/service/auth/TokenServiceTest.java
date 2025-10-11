package pl.kielce.tu.backend.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import pl.kielce.tu.backend.extractor.ClaimsExtractor;
import pl.kielce.tu.backend.mapper.TokenMapper;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.entity.BlacklistedToken;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.BlacklistedTokenRepository;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private TokenMapper tokenMapper;

    @Mock
    private CookieService cookieService;

    @Mock
    private ClaimsExtractor claimsExtractor;

    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @InjectMocks
    private TokenService tokenService;

    private final String secret = "01234567890123456789012345678901";

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(tokenService, "jwtSecret", secret);
        ReflectionTestUtils.setField(tokenService, "jwtExpirationMillis", 3600000);
        ReflectionTestUtils.setField(tokenService, "jwtRefreshExpirationMillis", 86400000);
    }

    @Test
    void generateToken_accessToken_shouldContainUserIdAndProperExpiration() {
        User user = new User();
        user.setId(42L);

        String token = tokenService.generateToken(user, CookieNames.ACCESS_TOKEN);
        assertNotNull(token);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("42", claims.getSubject());

        Date issuedAt = claims.getIssuedAt();
        Date expiresAt = claims.getExpiration();
        long diff = expiresAt.getTime() - issuedAt.getTime();
        assertTrue(Math.abs(diff - 3600000L) < 2000L, "Expiration should be approx jwtExpirationMillis");
    }

    @Test
    void extractUserIdFromToken_delegatesToClaimsExtractor() {
        String token = "dummy";
        when(claimsExtractor.extractUserId(token, secret)).thenReturn(123L);

        Long id = tokenService.extractUserIdFromToken(token);
        assertEquals(123L, id);
        verify(claimsExtractor).extractUserId(token, secret);
    }

    @Test
    void isTokenBlacklisted_returnsTrueWhenPresent() {
        String token = "t";
        when(blacklistedTokenRepository.findByToken(token)).thenReturn(Optional.of(new BlacklistedToken()));

        assertTrue(tokenService.isTokenBlacklisted(token));
        verify(blacklistedTokenRepository).findByToken(token);
    }

    @Test
    void isTokenBlacklisted_returnsFalseOnRepositoryException() {
        String token = "t";
        when(blacklistedTokenRepository.findByToken(token)).thenThrow(new RuntimeException("db"));

        assertFalse(tokenService.isTokenBlacklisted(token));
    }

    @Test
    void blacklistRequestTokens_savesBothTokensWhenPresent() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        String access = "accessTok";
        String refresh = "refreshTok";

        when(cookieService.getTokenFromCookie(req, CookieNames.ACCESS_TOKEN)).thenReturn(access);
        when(cookieService.getTokenFromCookie(req, CookieNames.REFRESH_TOKEN)).thenReturn(refresh);

        BlacklistedToken at = new BlacklistedToken();
        BlacklistedToken rt = new BlacklistedToken();

        when(tokenMapper.toBlacklistedToken(access)).thenReturn(at);
        when(tokenMapper.toBlacklistedToken(refresh)).thenReturn(rt);

        tokenService.blacklistRequestTokens(req);

        verify(tokenMapper).toBlacklistedToken(access);
        verify(tokenMapper).toBlacklistedToken(refresh);
        verify(blacklistedTokenRepository, times(2)).save(any(BlacklistedToken.class));
    }

    @Test
    void blacklistRequestTokens_skipsWhenNoTokenFound() {
        HttpServletRequest req = mock(HttpServletRequest.class);

        when(cookieService.getTokenFromCookie(req, CookieNames.ACCESS_TOKEN)).thenReturn(null);
        when(cookieService.getTokenFromCookie(req, CookieNames.REFRESH_TOKEN)).thenReturn("");

        tokenService.blacklistRequestTokens(req);

        verifyNoInteractions(tokenMapper);
        verifyNoInteractions(blacklistedTokenRepository);
    }
}
