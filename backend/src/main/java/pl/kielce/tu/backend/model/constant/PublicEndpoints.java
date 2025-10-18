package pl.kielce.tu.backend.model.constant;

import java.util.List;

import org.springframework.util.AntPathMatcher;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PublicEndpoints {
    AUTH_LOGIN("/api/v1/auth/login"),
    AUTH_REGISTER("/api/v1/auth/register"),
    AUTH_LOGOUT("/api/v1/auth/logout"),
    GENRES("/api/v1/genres"),
    API_DOCS("/v3/api-docs/**"),
    API_DOCS_LEGACY("/api-docs/**"),
    SWAGGER_UI("/swagger-ui/**"),
    SWAGGER_UI_HTML("/swagger-ui.html"),
    SWAGGER_RESOURCES("/swagger-resources/**"),
    ROOT_PATH("/");

    private final String pattern;
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    public static boolean isMember(String path) {
        String[] patterns = getAllPatterns();
        for (String pattern : patterns) {
            if (PATH_MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    public static String[] getAllPatterns() {
        return List.of(PublicEndpoints.values())
                .stream()
                .map(PublicEndpoints::getPattern)
                .toArray(String[]::new);
    }

}
