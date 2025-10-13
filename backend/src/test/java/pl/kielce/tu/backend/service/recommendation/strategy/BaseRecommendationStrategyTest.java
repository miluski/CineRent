package pl.kielce.tu.backend.service.recommendation.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import pl.kielce.tu.backend.model.constant.RecommendationConstants;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.util.UserContextLogger;

class BaseRecommendationStrategyTest {

    private TestableBaseRecommendationStrategy strategy;
    private UserContextLogger logger;
    private User user;
    private Dvd dvd1;
    private Dvd dvd2;

    @BeforeEach
    void setUp() {
        strategy = new TestableBaseRecommendationStrategy();
        logger = mock(UserContextLogger.class);

        user = new User();
        user.setId(1L);

        dvd1 = new Dvd();
        dvd1.setId(1L);
        dvd1.setTitle("Test Movie 1");

        dvd2 = new Dvd();
        dvd2.setId(2L);
        dvd2.setTitle("Test Movie 2");
    }

    @Test
    void recommend_shouldReturnEmptyList_whenUserIsNull() {
        List<Dvd> result = strategy.recommend(null, logger);
        assertTrue(result.isEmpty());
        verify(logger, never()).logUserOperation("RECOMMENDATION_ERROR", "Recommendation failed: " + "someError");
    }

    @Test
    void recommend_shouldReturnEmptyList_whenUserIdIsNull() {
        user.setId(null);
        List<Dvd> result = strategy.recommend(user, logger);
        assertTrue(result.isEmpty());
        verify(logger, never()).logUserOperation("RECOMMENDATION_ERROR", "Recommendation failed: " + "someError");
    }

    @Test
    void recommend_shouldReturnRecommendations_whenUserIsValid() {
        List<Dvd> expectedDvds = Arrays.asList(dvd1, dvd2);
        strategy.setRecommendationsToReturn(expectedDvds);
        List<Dvd> result = strategy.recommend(user, logger);
        assertEquals(expectedDvds, result);
        verify(logger, never()).logUserOperation("RECOMMENDATION_ERROR", "Recommendation failed: " + "someError");
    }

    @Test
    void recommend_shouldReturnEmptyList_whenExceptionOccurs() {
        strategy.setShouldThrowException(true);
        List<Dvd> result = strategy.recommend(user, logger);
        assertTrue(result.isEmpty());
        verify(logger, times(1)).logUserOperation("RECOMMENDATION_ERROR", "Recommendation failed: Test exception");
    }

    @Test
    void recommend_shouldHandleExceptionGracefully_whenLoggerIsNull() {
        strategy.setShouldThrowException(true);
        List<Dvd> result = strategy.recommend(user, null);
        assertTrue(result.isEmpty());
    }

    @Test
    void isValidUser_shouldReturnTrue_whenUserAndIdAreNotNull() {
        boolean result = strategy.isValidUser(user);
        assertTrue(result);
    }

    @Test
    void isValidUser_shouldReturnFalse_whenUserIsNull() {
        boolean result = strategy.isValidUser(null);
        assertFalse(result);
    }

    @Test
    void isValidUser_shouldReturnFalse_whenUserIdIsNull() {
        user.setId(null);
        boolean result = strategy.isValidUser(user);
        assertFalse(result);
    }

    @Test
    void createPageable_shouldReturnCorrectPageable() {
        Pageable result = strategy.createPageable();
        assertEquals(0, result.getPageNumber());
        assertEquals(RecommendationConstants.MAX_RECOMMENDATIONS.getValue(), result.getPageSize());
    }

    @Test
    @SuppressWarnings("unchecked")
    void extractContent_shouldReturnContent_whenPageIsNotNull() {
        Page<Dvd> page = mock(Page.class);
        List<Dvd> expectedContent = Arrays.asList(dvd1, dvd2);
        when(page.getContent()).thenReturn(expectedContent);
        List<Dvd> result = strategy.extractContent(page);
        assertEquals(expectedContent, result);
    }

    @Test
    void extractContent_shouldReturnEmptyList_whenPageIsNull() {
        List<Dvd> result = strategy.extractContent(null);
        assertTrue(result.isEmpty());
    }

    private static class TestableBaseRecommendationStrategy extends BaseRecommendationStrategy {
        private List<Dvd> recommendationsToReturn = Collections.emptyList();
        private boolean shouldThrowException = false;

        @Override
        protected List<Dvd> executeRecommendation(User user) {
            if (shouldThrowException) {
                throw new RuntimeException("Test exception");
            }
            return recommendationsToReturn;
        }

        @Override
        public int getPriority() {
            return 1;
        }

        @Override
        public String getReason() {
            return "TEST";
        }

        public void setRecommendationsToReturn(List<Dvd> recommendations) {
            this.recommendationsToReturn = recommendations;
        }

        public void setShouldThrowException(boolean shouldThrowException) {
            this.shouldThrowException = shouldThrowException;
        }

        @Override
        public boolean isValidUser(User user) {
            return super.isValidUser(user);
        }

        @Override
        public Pageable createPageable() {
            return super.createPageable();
        }

        @Override
        public List<Dvd> extractContent(Page<Dvd> page) {
            return super.extractContent(page);
        }
    }
}
