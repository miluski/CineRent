package pl.kielce.tu.backend.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kielce.tu.backend.filter.factory.ValidationStrategyFactory;
import pl.kielce.tu.backend.filter.strategy.ValidationResult;
import pl.kielce.tu.backend.filter.util.ResponseHelper;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.constant.ProtectedEndpoints;
import pl.kielce.tu.backend.model.constant.PublicEndpoints;
import pl.kielce.tu.backend.model.entity.User;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenRequestFilter extends OncePerRequestFilter {

    private final ResponseHelper responseHelper;
    private final ValidationStrategyFactory validationFactory;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getRequestURI();
        if (PublicEndpoints.isMember(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }
        processAuthentication(request, response, filterChain, requestPath);
    }

    private void processAuthentication(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain, String requestPath) throws IOException, ServletException {
        try {
            if (authenticateAndAuthorize(request, response, requestPath)) {
                filterChain.doFilter(request, response);
            }
        } catch (ExpiredJwtException e) {
            handleExpiredToken(response, requestPath);
        } catch (JwtException e) {
            handleInvalidToken(response, requestPath, e);
        } catch (NumberFormatException e) {
            handleInvalidTokenFormat(response, requestPath);
        } catch (Exception e) {
            handleUnexpectedError(response, requestPath, e);
        }
    }

    private boolean authenticateAndAuthorize(HttpServletRequest request, HttpServletResponse response,
            String requestPath) throws IOException {
        CookieNames tokenType = determineTokenType(requestPath);
        ValidationResult result = validationFactory
                .createValidationChain(request, response, requestPath, tokenType)
                .execute();
        if (result.isSuccess()) {
            authenticateUser((User) result.getData());
            return true;
        }
        return false;
    }

    private CookieNames determineTokenType(String requestPath) {
        return ProtectedEndpoints.REFRESH_TOKENS.matches(requestPath)
                ? CookieNames.REFRESH_TOKEN
                : CookieNames.ACCESS_TOKEN;
    }

    private void authenticateUser(User user) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getId(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRank().name())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void handleExpiredToken(HttpServletResponse response, String requestPath) throws IOException {
        log.debug("Token expired for {}", requestPath);
        responseHelper.sendUnauthorized(response, "Token expired");
    }

    private void handleInvalidToken(HttpServletResponse response, String requestPath, JwtException e)
            throws IOException {
        log.debug("Invalid token for {}: {}", requestPath, e.getMessage());
        responseHelper.sendUnauthorized(response, "Invalid token: " + e.getMessage());
    }

    private void handleInvalidTokenFormat(HttpServletResponse response, String requestPath) throws IOException {
        log.debug("Invalid token format for {}", requestPath);
        responseHelper.sendUnauthorized(response, "Invalid token format");
    }

    private void handleUnexpectedError(HttpServletResponse response, String requestPath, Exception e)
            throws IOException {
        log.error("Unexpected error during token validation for {}", requestPath, e);
        responseHelper.sendUnauthorized(response, "Authentication failed");
    }

}
