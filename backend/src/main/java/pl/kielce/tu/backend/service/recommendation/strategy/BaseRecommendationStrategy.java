package pl.kielce.tu.backend.service.recommendation.strategy;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import pl.kielce.tu.backend.model.constant.RecommendationConstants;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.util.UserContextLogger;

public abstract class BaseRecommendationStrategy implements RecommendationStrategy {

    @Override
    public List<Dvd> recommend(User user, UserContextLogger logger) {
        if (!isValidUser(user)) {
            return Collections.emptyList();
        }
        try {
            return executeRecommendation(user);
        } catch (Exception e) {
            logError("Recommendation failed", e, logger);
            return Collections.emptyList();
        }
    }

    protected abstract List<Dvd> executeRecommendation(User user);

    protected boolean isValidUser(User user) {
        return user != null && user.getId() != null;
    }

    protected Pageable createPageable() {
        return PageRequest.of(0, RecommendationConstants.MAX_RECOMMENDATIONS.getValue());
    }

    protected List<Dvd> extractContent(Page<Dvd> page) {
        return page != null ? page.getContent() : Collections.emptyList();
    }

    private void logError(String message, Exception e, UserContextLogger logger) {
        if (logger != null) {
            logger.logUserOperation("RECOMMENDATION_ERROR", message + ": " + e.getMessage());
        }
    }

}
