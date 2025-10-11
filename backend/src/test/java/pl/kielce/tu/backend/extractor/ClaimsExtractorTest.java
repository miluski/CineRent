package pl.kielce.tu.backend.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class ClaimsExtractorTest {

    private final ClaimsExtractor extractor = new ClaimsExtractor();

    @Test
    void extractUserId_returnsLongForNumericSubject() {
        String secret = "01234567890123456789012345678901";
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("42")
                .signWith(key)
                .compact();

        Long result = extractor.extractUserId(token, secret);

        assertEquals(42L, result);
    }

    @Test
    void extractUserId_throwsNumberFormatExceptionForNonNumericSubject() {
        String secret = "01234567890123456789012345678901";
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("not-a-number")
                .signWith(key)
                .compact();

        assertThrows(NumberFormatException.class, () -> extractor.extractUserId(token, secret));
    }

    @Test
    void extractUserId_throwsJwtExceptionForInvalidSignature() {
        String correctSecret = "01234567890123456789012345678901";
        SecretKey correctKey = Keys.hmacShaKeyFor(correctSecret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("7")
                .signWith(correctKey)
                .compact();

        String wrongSecret = "abcdefghijklmnopqrstuvwxyzabcdef";
        assertThrows(JwtException.class, () -> extractor.extractUserId(token, wrongSecret));
    }

    @Test
    void extractUserId_throwsJwtExceptionForMalformedToken() {
        String secret = "01234567890123456789012345678901";
        String malformedToken = "not.a.valid.jwt.token";

        assertThrows(JwtException.class, () -> extractor.extractUserId(malformedToken, secret));
    }

    @Test
    void extractUserId_throwsJwtExceptionForEmptyToken() {
        String secret = "01234567890123456789012345678901";
        String emptyToken = "";

        assertThrows(IllegalArgumentException.class, () -> extractor.extractUserId(emptyToken, secret));
    }

    @Test
    void extractUserId_throwsJwtExceptionForEmptySubject() {
        String secret = "01234567890123456789012345678901";
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("")
                .signWith(key)
                .compact();

        assertThrows(JwtException.class, () -> extractor.extractUserId(token, secret));
    }

    @Test
    void extractUserId_handlesLargeUserId() {
        String secret = "01234567890123456789012345678901";
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject(String.valueOf(Long.MAX_VALUE))
                .signWith(key)
                .compact();

        Long result = extractor.extractUserId(token, secret);

        assertEquals(Long.MAX_VALUE, result);
    }

    @Test
    void extractExpirationMillis_returnsCorrectDifference() {
        String secret = "01234567890123456789012345678901";
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        long issuedAtMillis = Instant.now().toEpochMilli();
        long expiresAtMillis = issuedAtMillis + 3600000L;

        String token = Jwts.builder()
                .subject("1")
                .issuedAt(new Date(issuedAtMillis))
                .expiration(new Date(expiresAtMillis))
                .signWith(key)
                .compact();

        long result = extractor.extractExpirationMillis(token, secret);

        assertEquals(3600000L, result);
    }

    @Test
    void extractExpirationMillis_throwsJwtExceptionForInvalidSignature() {
        String correctSecret = "01234567890123456789012345678901";
        SecretKey correctKey = Keys.hmacShaKeyFor(correctSecret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .subject("1")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000L))
                .signWith(correctKey)
                .compact();

        String wrongSecret = "abcdefghijklmnopqrstuvwxyzabcdef";
        assertThrows(JwtException.class, () -> extractor.extractExpirationMillis(token, wrongSecret));
    }

    @Test
    void extractExpirationMillis_throwsJwtExceptionForMalformedToken() {
        String secret = "01234567890123456789012345678901";
        String malformedToken = "not.a.valid.jwt.token";

        assertThrows(JwtException.class, () -> extractor.extractExpirationMillis(malformedToken, secret));
    }

    @Test
    void extractExpirationMillis_handlesShortExpiration() {
        String secret = "01234567890123456789012345678901";
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        long issuedAtMillis = Instant.now().toEpochMilli();
        long expiresAtMillis = issuedAtMillis + 1000L;

        String token = Jwts.builder()
                .subject("1")
                .issuedAt(new Date(issuedAtMillis))
                .expiration(new Date(expiresAtMillis))
                .signWith(key)
                .compact();

        long result = extractor.extractExpirationMillis(token, secret);

        assertEquals(1000L, result);
    }

    @Test
    void extractExpirationMillis_handlesLongExpiration() {
        String secret = "01234567890123456789012345678901";
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        long issuedAtMillis = Instant.now().toEpochMilli();
        long expiresAtMillis = issuedAtMillis + 2592000000L;

        String token = Jwts.builder()
                .subject("1")
                .issuedAt(new Date(issuedAtMillis))
                .expiration(new Date(expiresAtMillis))
                .signWith(key)
                .compact();

        long result = extractor.extractExpirationMillis(token, secret);

        assertEquals(2592000000L, result);
    }

    @Test
    void extractClaims_handlesTokenWithoutIssuedAt() {
        String secret = "01234567890123456789012345678901";
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .subject("123")
                .expiration(new Date(System.currentTimeMillis() + 3600000L))
                .signWith(key)
                .compact();

        Long result = extractor.extractUserId(token, secret);

        assertEquals(123L, result);
    }
}
