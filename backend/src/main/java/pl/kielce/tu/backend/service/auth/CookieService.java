package pl.kielce.tu.backend.service.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.kielce.tu.backend.model.constant.CookieNames;

@Service
public class CookieService {

    @Value("${jwt.cookie.maxAge}")
    private int cookieMaxAge;

    @Value("${jwt.refresh.cookie.maxAge}")
    private int refreshCookieMaxAge;

    public void setAccessTokenCookie(HttpServletResponse response, String token) {
        setTokenCookie(response, token, CookieNames.ACCESS_TOKEN, cookieMaxAge);
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String token) {
        setTokenCookie(response, token, CookieNames.REFRESH_TOKEN, refreshCookieMaxAge);
    }

    public void deleteTokenCookie(HttpServletResponse response, CookieNames cookieName) {
        setTokenCookie(response, "", cookieName, 0);
    }

    private void setTokenCookie(HttpServletResponse response, String token, CookieNames cookieName, int maxAge) {
        Cookie cookie = new Cookie(cookieName.name(), token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(token.isEmpty() ? 0 : maxAge);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public String getTokenFromCookie(HttpServletRequest request, CookieNames cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                String enumCookieName = cookieName.name();
                String cookieRetrievedName = cookie.getName();
                if (enumCookieName.equals(cookieRetrievedName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
