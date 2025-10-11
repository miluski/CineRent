package pl.kielce.tu.backend.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.kielce.tu.backend.model.constant.CookieNames;

@ExtendWith(MockitoExtension.class)
class CookieServiceTest {

    private CookieService cookieService;

    @BeforeEach
    void setUp() throws Exception {
        cookieService = new CookieService();
        setPrivateIntField("cookieMaxAge", 3600);
        setPrivateIntField("refreshCookieMaxAge", 7200);
    }

    private void setPrivateIntField(String name, int value) throws Exception {
        Field field = CookieService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.setInt(cookieService, value);
    }

    @Test
    void setAccessTokenCookie_shouldAddCookieWithProperAttributes() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);

        cookieService.setAccessTokenCookie(response, "access-token-123");

        verify(response, times(1)).addCookie(captor.capture());
        Cookie added = captor.getValue();

        assertEquals(CookieNames.ACCESS_TOKEN.name(), added.getName());
        assertEquals("access-token-123", added.getValue());
        assertTrue(added.isHttpOnly());
        assertTrue(added.getSecure());
        assertEquals(3600, added.getMaxAge());
        assertEquals("/", added.getPath());
    }

    @Test
    void setRefreshTokenCookie_shouldUseRefreshCookieMaxAge() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);

        cookieService.setRefreshTokenCookie(response, "refresh-token-1");

        verify(response).addCookie(captor.capture());
        Cookie added = captor.getValue();

        assertEquals(CookieNames.REFRESH_TOKEN.name(), added.getName());
        assertEquals("refresh-token-1", added.getValue());
        assertEquals(7200, added.getMaxAge());
    }

    @Test
    void deleteTokenCookie_shouldAddEmptyCookieWithZeroMaxAge() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);

        cookieService.deleteTokenCookie(response, CookieNames.ACCESS_TOKEN);

        verify(response).addCookie(captor.capture());
        Cookie added = captor.getValue();

        assertEquals(CookieNames.ACCESS_TOKEN.name(), added.getName());
        assertEquals("", added.getValue());
        assertEquals(0, added.getMaxAge());
    }

    @Test
    void getTokenFromCookie_shouldReturnValueWhenPresent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie present = new Cookie(CookieNames.ACCESS_TOKEN.name(), "found-token");
        when(request.getCookies()).thenReturn(new Cookie[] { present });

        String token = cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN);
        assertEquals("found-token", token);
    }

    @Test
    void getTokenFromCookie_shouldReturnNullWhenNoCookies() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        String token = cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN);
        assertNull(token);
    }

    @Test
    void getTokenFromCookie_shouldReturnNullWhenCookieMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie other = new Cookie("OTHER_COOKIE", "val");
        when(request.getCookies()).thenReturn(new Cookie[] { other });

        String token = cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN);
        assertNull(token);
    }
}
