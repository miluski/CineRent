package pl.kielce.tu.backend.interceptor;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.kielce.tu.backend.model.constant.RequestTrackingConstants;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class EndpointLoggingInterceptorTest {

    @Mock
    private UserContextLogger userContextLogger;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private EndpointLoggingInterceptor interceptor;

    @Test
    void preHandle_shouldLogStartedAndSetStartTime_withQueryString() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getQueryString()).thenReturn("a=1");

        boolean result = interceptor.preHandle(request, response, new Object());
        assert result;

        verify(userContextLogger).logEndpointAccess(
                eq("GET"),
                eq("/api/test?a=1"),
                eq((String) RequestTrackingConstants.STARTED_STATUS.getValue()));

        verify(request).setAttribute(eq((String) RequestTrackingConstants.START_TIME_ATTR.getValue()), anyLong());
    }

    @Test
    void afterCompletion_shouldLogStatusWithException_andCalculateDuration() {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/submit");
        when(response.getStatus()).thenReturn(500);

        long start = System.currentTimeMillis() - 50;
        when(request.getAttribute((String) RequestTrackingConstants.START_TIME_ATTR.getValue())).thenReturn(start);

        RuntimeException ex = new RuntimeException("boom");

        interceptor.afterCompletion(request, response, new Object(), ex);

        verify(userContextLogger).logEndpointAccess(
                eq("POST"),
                eq("/submit"),
                eq("500 (Exception: RuntimeException)"));
    }

    @Test
    void afterCompletion_shouldDetectSlowRequest_andLogUserOperation() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/slow");
        when(response.getStatus()).thenReturn(200);

        long threshold = (Long) RequestTrackingConstants.SLOW_REQUEST_THRESHOLD_MS.getValue();
        long start = System.currentTimeMillis() - (threshold + 10);
        when(request.getAttribute((String) RequestTrackingConstants.START_TIME_ATTR.getValue())).thenReturn(start);

        interceptor.afterCompletion(request, response, new Object(), null);

        verify(userContextLogger).logEndpointAccess(
                eq("GET"),
                eq("/slow"),
                eq("200"));

        verify(userContextLogger).logUserOperation(
                eq("SLOW_REQUEST_DETECTED"),
                argThat(message -> message.contains("Request GET /slow took") && message.contains("ms")));
    }
}
