package pl.kielce.tu.backend.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoggingConstants {

    USER_PREFIX("user:"),
    ANONYMOUS_USER("anonymous"),
    LOG_SEPARATOR(" | ");

    private final String value;
}
