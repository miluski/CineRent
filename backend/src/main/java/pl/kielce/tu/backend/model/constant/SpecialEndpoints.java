package pl.kielce.tu.backend.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SpecialEndpoints {
    REFRESH_TOKENS("/api/v1/auth/refresh-tokens");

    private final String pattern;

    public boolean matches(String path) {
        return pattern.equals(path);
    }
}
