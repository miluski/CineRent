package pl.kielce.tu.backend.service.recommendation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.HttpServletRequest;
import pl.kielce.tu.backend.extractor.ClaimsExtractor;
import pl.kielce.tu.backend.mapper.RecommendationMapper;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.dto.DvdDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.UserRepository;
import pl.kielce.tu.backend.service.auth.CookieService;
import pl.kielce.tu.backend.service.recommendation.strategy.RecommendationStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private CookieService cookieService;

    @Mock
    private ClaimsExtractor claimsExtractor;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecommendationMapper recommendationMapper;

    @Mock
    private RecommendationStrategy strategy1;

    @Mock
    private RecommendationStrategy strategy2;

    @Mock
    private HttpServletRequest request;

    @Mock
    private UserContextLogger userContextLogger;

    private RecommendationService recommendationService;

    private final String jwtSecret = "testSecret";

    @BeforeEach
    void setUp() {
        List<RecommendationStrategy> strategies = Arrays.asList(strategy1, strategy2);
        recommendationService = new RecommendationService(cookieService, userRepository,
                claimsExtractor, userContextLogger, strategies, recommendationMapper);
        ReflectionTestUtils.setField(recommendationService, "jwtSecret", jwtSecret);
    }

    @Test
    void shouldHandleGetDvdRecommendations_successfullyGenerateRecommendations() {
        Long userId = 1L;
        String token = "validToken";
        User user = createTestUser(userId);
        List<Dvd> dvds = createTestDvds();
        List<DvdDto> expectedDtos = createTestDvdDtos();

        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(token);
        when(claimsExtractor.extractUserId(token, jwtSecret)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(strategy1.getPriority()).thenReturn(1);
        when(strategy2.getPriority()).thenReturn(2);
        when(strategy1.recommend(eq(user), any())).thenReturn(dvds);
        when(strategy2.recommend(eq(user), any())).thenReturn(Collections.emptyList());
        when(strategy1.getReason()).thenReturn("Test reason 1");
        when(recommendationMapper.mapToRecommendationDtos(eq(dvds), anyString(), any())).thenReturn(expectedDtos);

        ResponseEntity<List<DvdDto>> response = recommendationService.handleGetDvdRecommendations(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<DvdDto> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(2, responseBody.size());
        assertEquals("Test reason 1", responseBody.get(0).getRecommendationReason());
        verify(userRepository).findById(userId);
        verify(strategy1).recommend(eq(user), any());
    }

    @Test
    void shouldHandleGetDvdRecommendations_returnInternalServerError_whenTokenNotFound() {
        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(null);
        ResponseEntity<List<DvdDto>> response = recommendationService.handleGetDvdRecommendations(request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(claimsExtractor, never()).extractUserId(anyString(), anyString());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void shouldHandleGetDvdRecommendations_returnInternalServerError_whenUserNotFound() {
        Long userId = 1L;
        String token = "validToken";

        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(token);
        when(claimsExtractor.extractUserId(token, jwtSecret)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseEntity<List<DvdDto>> response = recommendationService.handleGetDvdRecommendations(request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userRepository).findById(userId);
    }

    @Test
    void shouldGenerateRecommendations_limitToTwentyResults() {
        Long userId = 1L;
        String token = "validToken";
        User user = createTestUser(userId);
        List<Dvd> manyDvds = createManyTestDvds(25);

        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(token);
        when(claimsExtractor.extractUserId(token, jwtSecret)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(strategy1.getPriority()).thenReturn(1);
        when(strategy2.getPriority()).thenReturn(2);
        when(strategy1.recommend(eq(user), any())).thenReturn(manyDvds);
        lenient().when(strategy2.recommend(eq(user), any())).thenReturn(Collections.emptyList());
        when(strategy1.getReason()).thenReturn("Test reason");
        when(recommendationMapper.mapToRecommendationDtos(eq(manyDvds), anyString(), any()))
                .thenReturn(createManyTestDtos());
        ResponseEntity<List<DvdDto>> response = recommendationService.handleGetDvdRecommendations(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<DvdDto> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(20, responseBody.size());
    }

    @Test
    void shouldHandleGetDvdRecommendations_returnEmptyList_whenNoRecommendations() {
        Long userId = 1L;
        String token = "validToken";
        User user = createTestUser(userId);

        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(token);
        when(claimsExtractor.extractUserId(token, jwtSecret)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(strategy1.getPriority()).thenReturn(1);
        when(strategy2.getPriority()).thenReturn(2);
        when(strategy1.recommend(eq(user), any())).thenReturn(Collections.emptyList());
        when(strategy2.recommend(eq(user), any())).thenReturn(Collections.emptyList());

        ResponseEntity<List<DvdDto>> response = recommendationService.handleGetDvdRecommendations(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<DvdDto> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(0, responseBody.size());
    }

    private User createTestUser(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setAge(25);
        return user;
    }

    private List<Dvd> createTestDvds() {
        Dvd dvd1 = new Dvd();
        dvd1.setId(1L);
        dvd1.setTitle("Test Movie 1");

        Dvd dvd2 = new Dvd();
        dvd2.setId(2L);
        dvd2.setTitle("Test Movie 2");

        return Arrays.asList(dvd1, dvd2);
    }

    private List<DvdDto> createTestDvdDtos() {
        DvdDto dto1 = new DvdDto();
        dto1.setId(1L);
        dto1.setTitle("Test Movie 1");
        dto1.setRecommendationReason("Test reason 1");

        DvdDto dto2 = new DvdDto();
        dto2.setId(2L);
        dto2.setTitle("Test Movie 2");
        dto2.setRecommendationReason("Test reason 1");

        return Arrays.asList(dto1, dto2);
    }

    private List<Dvd> createManyTestDvds(int count) {
        List<Dvd> dvds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Dvd dvd = new Dvd();
            dvd.setId((long) i + 1);
            dvd.setTitle("Test Movie " + (i + 1));
            dvds.add(dvd);
        }
        return dvds;
    }

    private List<DvdDto> createManyTestDtos() {
        List<DvdDto> dtos = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            DvdDto dto = new DvdDto();
            dto.setId((long) i + 1);
            dto.setTitle("Test Movie " + (i + 1));
            dto.setRecommendationReason("Test reason");
            dtos.add(dto);
        }
        return dtos;
    }

}
