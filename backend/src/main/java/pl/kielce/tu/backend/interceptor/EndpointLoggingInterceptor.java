package pl.kielce.tu.backend.interceptor;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.RequestInfo;
import pl.kielce.tu.backend.model.constant.RequestTrackingConstants;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class EndpointLoggingInterceptor implements HandlerInterceptor {

    private final UserContextLogger userContextLogger;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        String fullEndpoint = buildFullEndpoint(request);
        userContextLogger.logEndpointAccess(request.getMethod(), fullEndpoint,
                (String) RequestTrackingConstants.STARTED_STATUS.getValue());
        request.setAttribute((String) RequestTrackingConstants.START_TIME_ATTR.getValue(), System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler, @Nullable Exception ex) {
        RequestInfo requestInfo = extractRequestInfo(request, response, ex);
        userContextLogger.logEndpointAccess(requestInfo.method(), requestInfo.endpoint(), requestInfo.statusInfo());
        logPerformanceIfNeeded(requestInfo);
    }

    private String buildFullEndpoint(HttpServletRequest request) {
        String endpoint = request.getRequestURI();
        String queryString = request.getQueryString();
        return queryString != null ? endpoint + "?" + queryString : endpoint;
    }

    private RequestInfo extractRequestInfo(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        String method = request.getMethod();
        String endpoint = request.getRequestURI();
        String statusInfo = buildStatusInfo(response.getStatus(), ex);
        long duration = calculateDuration(request);
        return new RequestInfo(method, endpoint, statusInfo, duration);
    }

    private String buildStatusInfo(int status, Exception ex) {
        String statusInfo = String.valueOf(status);
        return ex != null ? statusInfo + " (Exception: " + ex.getClass().getSimpleName() + ")" : statusInfo;
    }

    private long calculateDuration(HttpServletRequest request) {
        Long startTime = (Long) request.getAttribute((String) RequestTrackingConstants.START_TIME_ATTR.getValue());
        return startTime != null ? System.currentTimeMillis() - startTime : 0;
    }

    private void logPerformanceIfNeeded(RequestInfo requestInfo) {
        if (requestInfo.duration() > (Long) RequestTrackingConstants.SLOW_REQUEST_THRESHOLD_MS.getValue()) {
            userContextLogger.logUserOperation("SLOW_REQUEST_DETECTED",
                    String.format("Request %s %s took %sms",
                            requestInfo.method(), requestInfo.endpoint(), requestInfo.duration()));
        } else {
            userContextLogger.logUserOperation("REQUEST_COMPLETED",
                    String.format("Request %s %s completed in %sms",
                            requestInfo.method(), requestInfo.endpoint(), requestInfo.duration()));
        }
    }

}
