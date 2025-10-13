package pl.kielce.tu.backend.service.recommendation.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.DvdRepository;
import pl.kielce.tu.backend.repository.RentalRepository;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class PopularRecommendationStrategyTest {

    @Mock
    private DvdRepository dvdRepository;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private UserContextLogger userContextLogger;

    private PopularRecommendationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PopularRecommendationStrategy(dvdRepository, rentalRepository);
    }

    @Test
    void shouldRecommend_returnPopularDvds_whenRentalsExist() {
        User user = createTestUser();
        List<Dvd> expectedDvds = createTestDvds();
        Page<Dvd> page = new PageImpl<>(expectedDvds);

        when(rentalRepository.count()).thenReturn(10L);
        when(dvdRepository.findMostPopularAvailableDvds(any(Pageable.class))).thenReturn(page);

        List<Dvd> result = strategy.recommend(user, userContextLogger);

        assertEquals(3, result.size());
        assertEquals(expectedDvds, result);
    }

    @Test
    void shouldRecommend_returnEmptyList_whenNoRentalsExist() {
        User user = createTestUser();

        when(rentalRepository.count()).thenReturn(0L);

        List<Dvd> result = strategy.recommend(user, userContextLogger);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRecommend_returnEmptyList_whenRentalsExistButNoPopularDvds() {
        User user = createTestUser();
        Page<Dvd> emptyPage = new PageImpl<>(List.of());

        when(rentalRepository.count()).thenReturn(5L);
        when(dvdRepository.findMostPopularAvailableDvds(any(Pageable.class))).thenReturn(emptyPage);

        List<Dvd> result = strategy.recommend(user, userContextLogger);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetPriority_returnCorrectValue() {
        int priority = strategy.getPriority();
        assertEquals(4, priority);
    }

    @Test
    void shouldGetReason_returnCorrectMessage() {
        String reason = strategy.getReason();
        assertEquals("Rekomendacje wygenerowane na podstawie najbardziej popularnych gatunków filmów.", reason);
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setAge(30);
        return user;
    }

    private List<Dvd> createTestDvds() {
        Dvd dvd1 = new Dvd();
        dvd1.setId(1L);
        dvd1.setTitle("Most Popular Movie 1");

        Dvd dvd2 = new Dvd();
        dvd2.setId(2L);
        dvd2.setTitle("Most Popular Movie 2");

        Dvd dvd3 = new Dvd();
        dvd3.setId(3L);
        dvd3.setTitle("Most Popular Movie 3");

        return Arrays.asList(dvd1, dvd2, dvd3);
    }
}
