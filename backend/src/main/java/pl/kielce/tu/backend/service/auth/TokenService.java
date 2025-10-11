package pl.kielce.tu.backend.service.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kielce.tu.backend.extractor.ClaimsExtractor;
import pl.kielce.tu.backend.mapper.TokenMapper;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.entity.BlacklistedToken;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.BlacklistedTokenRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMillis;

    @Value("${jwt.refresh.expiration}")
    private int jwtRefreshExpirationMillis;

    private final TokenMapper tokenMapper;
    private final CookieService cookieService;
    private final ClaimsExtractor claimsExtractor;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public String generateToken(User user, CookieNames cookieName) {
        try {
            int expirationMillis = determineExpirationTime(cookieName);
            SecretKey key = createSecretKey();
            return buildJwtToken(user, expirationMillis, key);
        } catch (Exception e) {
            log.error("Error generating token for user: {}", user.getId(), e);
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    public Long extractUserIdFromToken(String token) {
        return claimsExtractor.extractUserId(token, jwtSecret);
    }

    public boolean isTokenBlacklisted(String token) {
        try {
            return blacklistedTokenRepository.findByToken(token).isPresent();
        } catch (Exception e) {
            log.error("Error checking if token is blacklisted", e);
            return false;
        }
    }

    public void blacklistRequestTokens(HttpServletRequest httpServletRequest) {
        blacklistToken(httpServletRequest, CookieNames.ACCESS_TOKEN);
        blacklistToken(httpServletRequest, CookieNames.REFRESH_TOKEN);
    }

    private int determineExpirationTime(CookieNames cookieName) {
        return CookieNames.ACCESS_TOKEN.equals(cookieName)
                ? jwtExpirationMillis
                : jwtRefreshExpirationMillis;
    }

    private SecretKey createSecretKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String buildJwtToken(User user, int expirationMillis, SecretKey key) {
        Date issuedAt = Date.from(Instant.now());
        Date expiresAt = Date.from(Instant.now().plusMillis(expirationMillis));
        String subject = String.valueOf(user.getId());

        return Jwts.builder()
                .subject(subject)
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(key)
                .compact();
    }

    private void blacklistToken(HttpServletRequest httpServletRequest, CookieNames cookieName) {
        try {
            String token = cookieService.getTokenFromCookie(httpServletRequest, cookieName);
            if (token == null || token.isEmpty()) {
                log.debug("No token found for cookie: {}", cookieName);
                return;
            }
            BlacklistedToken blacklistedToken = tokenMapper.toBlacklistedToken(token);
            blacklistedTokenRepository.save(blacklistedToken);
            log.info("Token blacklisted successfully for cookie: {}", cookieName);
        } catch (Exception e) {
            log.error("Error blacklisting token for cookie: {}", cookieName, e);
        }
    }

}
