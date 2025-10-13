package pl.kielce.tu.backend.service.recommendation.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.Genre;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.DvdRepository;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class PreferredGenreRecommendationStrategyTest {

    @Mock
    private DvdRepository dvdRepository;

    @Mock
    private UserContextLogger userContextLogger;

    private PreferredGenreRecommendationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PreferredGenreRecommendationStrategy(dvdRepository);
    }

    @Test
    void shouldRecommend_returnDvdsByPreferredGenres_whenUserHasPreferences() {
        User user = createUserWithPreferredGenres();
        List<Dvd> expectedDvds = createTestDvds();
        Page<Dvd> page = new PageImpl<>(expectedDvds);

        when(dvdRepository.findByPreferredGenresAndAvailable(eq(user.getPreferredGenres()), any(Pageable.class)))
                .thenReturn(page);
        List<Dvd> result = strategy.recommend(user, userContextLogger);
        assertEquals(2, result.size());
        assertEquals(expectedDvds, result);
    }

    @Test
    void shouldRecommend_returnEmptyList_whenUserHasNoPreferredGenres() {
        User user = createUserWithoutPreferredGenres();
        List<Dvd> result = strategy.recommend(user, userContextLogger);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRecommend_returnEmptyList_whenPreferredGenresAreNull() {
        User user = createUserWithNullPreferredGenres();
        List<Dvd> result = strategy.recommend(user, userContextLogger);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRecommend_returnEmptyList_whenNoDvdsMatchPreferences() {
        User user = createUserWithPreferredGenres();
        Page<Dvd> emptyPage = new PageImpl<>(Collections.emptyList());

        when(dvdRepository.findByPreferredGenresAndAvailable(eq(user.getPreferredGenres()), any(Pageable.class)))
                .thenReturn(emptyPage);
        List<Dvd> result = strategy.recommend(user, userContextLogger);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetPriority_returnCorrectValue() {
        int priority = strategy.getPriority();
        assertEquals(3, priority);
    }

    @Test
    void shouldGetReason_returnCorrectMessage() {
        String reason = strategy.getReason();

        assertEquals("Rekomendacje wygenerowane na podstawie twoich preferowanych gatunków filmów.", reason);
    }

    private User createUserWithPreferredGenres() {
        User user = new User();
        user.setId(1L);
        user.setAge(30);

        Genre action = new Genre();
        action.setId(1L);
        action.setName("Action");

        Genre comedy = new Genre();
        comedy.setId(2L);
        comedy.setName("Comedy");

        List<Genre> preferredGenres = Arrays.asList(action, comedy);
        user.setPreferredGenres(preferredGenres);

        return user;
    }

    private User createUserWithoutPreferredGenres() {
        User user = new User();
        user.setId(2L);
        user.setAge(25);
        user.setPreferredGenres(Collections.emptyList());
        return user;
    }

    private User createUserWithNullPreferredGenres() {
        User user = new User();
        user.setId(3L);
        user.setAge(35);
        user.setPreferredGenres(null);
        return user;
    }

    private List<Dvd> createTestDvds() {
        Dvd dvd1 = new Dvd();
        dvd1.setId(1L);
        dvd1.setTitle("Action Movie");

        Dvd dvd2 = new Dvd();
        dvd2.setId(2L);
        dvd2.setTitle("Comedy Movie");

        return Arrays.asList(dvd1, dvd2);
    }

}
