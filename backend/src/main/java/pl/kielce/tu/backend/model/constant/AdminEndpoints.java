package pl.kielce.tu.backend.model.constant;

import java.util.List;

import org.springframework.util.AntPathMatcher;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AdminEndpoints {
    ;
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
        return List.of(AdminEndpoints.values())
                .stream()
                .map(AdminEndpoints::getPattern)
                .toArray(String[]::new);
    }
}
