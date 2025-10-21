package pl.kielce.tu.backend.model.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RecommendationConstants {
    MAX_RECOMMENDATIONS(5),
    MIN_AGE_RANGE(5),
    MAX_AGE_RANGE(5),
    MIN_ADULT_AGE(18);

    private final int value;
}
