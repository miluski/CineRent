package pl.kielce.tu.backend.model.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ValidationConstraints {

    MIN_NICKNAME_LENGTH(3),
    MAX_NICKNAME_LENGTH(50),
    MIN_PASSWORD_LENGTH(8),
    MAX_PASSWORD_LENGTH(100);

    private final int value;

}
