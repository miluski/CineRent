package pl.kielce.tu.backend.extractor;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class ClaimsExtractor {

    public Long extractUserId(String token, String secret) {
        Claims claims = extractClaims(token, secret);
        String subject = claims.getSubject();
        return Long.parseLong(subject);
    }

    public long extractExpirationMillis(String token, String secret) {
        Claims claims = extractClaims(token, secret);
        long issuedAtMillis = claims.getIssuedAt().getTime();
        long expiresAtMillis = claims.getExpiration().getTime();
        return expiresAtMillis - issuedAtMillis;
    }

    public boolean extractIsRemembered(String token, String secret) {
        Claims claims = extractClaims(token, secret);
        Boolean isRemembered = claims.get("isRemembered", Boolean.class);
        return isRemembered != null && isRemembered;
    }

    private Claims extractClaims(String token, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
