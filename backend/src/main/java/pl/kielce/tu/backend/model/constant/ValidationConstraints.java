package pl.kielce.tu.backend.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ValidationConstraints {

    MIN_NICKNAME_LENGTH(3, null),
    MAX_NICKNAME_LENGTH(50, null),
    MIN_PASSWORD_LENGTH(8, null),
    MAX_PASSWORD_LENGTH(100, null),
    MIN_AGE(1, null),
    MAX_AGE(149, null),
    NICKNAME_PATTERN(0, "^[a-zA-Z0-9_-]+$");

    private final int value;
    private final String pattern;

}
