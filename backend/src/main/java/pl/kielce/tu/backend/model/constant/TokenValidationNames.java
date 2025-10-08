package pl.kielce.tu.backend.model.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenValidationNames {
    PRESENCE,
    BLACKLIST,
    EXTRACTION,
    USER_EXISTENCE,
    ADMIN_ACCESS;
}
