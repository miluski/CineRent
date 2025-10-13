package pl.kielce.tu.backend.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecommendationReason {

    USER_HISTORY("Rekomendacje bazowane na podstawie Twojej aktywności"),
    AGE_GROUP("Rekomendacje bazowane na podstawie wyborów Twojej grupy wiekowej."),
    PREFERRED_GENRES("Rekomendacje wygenerowane na podstawie twoich preferowanych gatunków filmów."),
    POPULAR("Rekomendacje wygenerowane na podstawie najbardziej popularnych gatunków filmów.");

    private final String message;

}
