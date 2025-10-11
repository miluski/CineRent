package pl.kielce.tu.backend.filter.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class ResponseHelperTest {

    @Mock
    private HttpServletResponse response;

    private final ResponseHelper helper = new ResponseHelper();

    @Test
    void sendUnauthorized_writesJsonErrorAndSetsStatusAndContentType() throws IOException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        String message = "Not authenticated";
        helper.sendUnauthorized(response, message);

        pw.flush();
        String expectedBody = "{\"error\": \"" + message + "\"}";
        assertEquals(expectedBody, sw.toString());

        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType("application/json");
    }

    @Test
    void sendForbidden_writesJsonErrorAndSetsStatusAndContentType() throws IOException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        String message = "Access denied";
        helper.sendForbidden(response, message);

        pw.flush();
        String expectedBody = "{\"error\": \"" + message + "\"}";
        assertEquals(expectedBody, sw.toString());

        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        verify(response).setContentType("application/json");
    }
}
