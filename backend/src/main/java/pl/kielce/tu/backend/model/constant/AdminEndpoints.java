package pl.kielce.tu.backend.model.constant;

import java.util.List;

import org.springframework.util.AntPathMatcher;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AdminEndpoints {
    DVD_CREATE("/api/v1/dvd/create"),
    DVD_PATCH("/api/v1/dvd/*/edit"),
    GENRE_CREATE("/api/v1/genres/create"),
    GENRE_DELETE("/api/v1/genres/*/delete"),
    RESERVATIONS_ACCEPT("/api/v1/reservations/*/accept"),
    RESERVATIONS_DECLINE("/api/v1/reservations/*/decline"),
    RESERVATIONS_ADMIN_ALL("/api/v1/reservations/all"),
    RETURN_ACCEPT("/api/v1/rentals/*/return-accept"),
    RETURN_DECLINE("/api/v1/rentals/*/return-decline"),
    RENTALS_ADMIN_RETURN_REQUESTS("/api/v1/rentals/return-requests"),
    TRANSACTIONS_ALL("/api/v1/transactions/all");

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
