package pl.kielce.tu.backend.service.recommendation.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
class AgeGroupRecommendationStrategyTest {

    @Mock
    private DvdRepository dvdRepository;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private UserContextLogger userContextLogger;

    private AgeGroupRecommendationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new AgeGroupRecommendationStrategy(dvdRepository, rentalRepository);
    }

    @Test
    void shouldRecommend_returnPopularDvdsInAgeGroup_whenRentalsExist() {
        User user = createTestUser(30);
        List<Dvd> expectedDvds = createTestDvds();
        Page<Dvd> page = new PageImpl<>(expectedDvds);

        when(rentalRepository.count()).thenReturn(5L);
        when(dvdRepository.findMostPopularDvdsByAgeGroup(eq(25), eq(35), any(Pageable.class))).thenReturn(page);

        List<Dvd> result = strategy.recommend(user, userContextLogger);

        assertEquals(2, result.size());
        assertEquals(expectedDvds, result);
    }

    @Test
    void shouldRecommend_returnEmptyList_whenNoRentalsExist() {
        User user = createTestUser(25);

        when(rentalRepository.count()).thenReturn(0L);

        List<Dvd> result = strategy.recommend(user, userContextLogger);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRecommend_returnEmptyList_whenNoAgeGroupData() {
        User user = createTestUser(20);
        Page<Dvd> emptyPage = new PageImpl<>(List.of());

        when(rentalRepository.count()).thenReturn(5L);
        when(dvdRepository.findMostPopularDvdsByAgeGroup(eq(15), eq(25), any(Pageable.class))).thenReturn(emptyPage);

        List<Dvd> result = strategy.recommend(user, userContextLogger);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRecommend_calculateCorrectAgeRange() {
        User user = createTestUser(40);
        List<Dvd> expectedDvds = createTestDvds();
        Page<Dvd> page = new PageImpl<>(expectedDvds);

        when(rentalRepository.count()).thenReturn(10L);
        when(dvdRepository.findMostPopularDvdsByAgeGroup(eq(35), eq(45), any(Pageable.class))).thenReturn(page);

        List<Dvd> result = strategy.recommend(user, userContextLogger);

        assertEquals(2, result.size());
        assertEquals(expectedDvds, result);
    }

    @Test
    void shouldGetPriority_returnCorrectValue() {
        int priority = strategy.getPriority();
        assertEquals(2, priority);
    }

    @Test
    void shouldGetReason_returnCorrectMessage() {
        String reason = strategy.getReason();

        assertEquals("Rekomendacje bazowane na podstawie wyborów Twojej grupy wiekowej.", reason);
    }

    private User createTestUser(int age) {
        User user = new User();
        user.setId(1L);
        user.setAge(age);
        return user;
    }

    private List<Dvd> createTestDvds() {
        Dvd dvd1 = new Dvd();
        dvd1.setId(1L);
        dvd1.setTitle("Popular Movie 1");

        Dvd dvd2 = new Dvd();
        dvd2.setId(2L);
        dvd2.setTitle("Popular Movie 2");

        return Arrays.asList(dvd1, dvd2);
    }
}
