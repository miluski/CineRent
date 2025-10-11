package pl.kielce.tu.backend.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RequestTrackingConstants {

    START_TIME_ATTR("startTime"),
    STARTED_STATUS("STARTED"),
    SLOW_REQUEST_THRESHOLD_MS(1000L);

    private final Object value;
}
