package pl.kielce.tu.backend.filter.util;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;

@Component
public class ResponseHelper {

    public void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        sendErrorResponse(response, HttpStatus.UNAUTHORIZED, message);
    }

    public void sendForbidden(HttpServletResponse response, String message) throws IOException {
        sendErrorResponse(response, HttpStatus.FORBIDDEN, message);
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }

}
