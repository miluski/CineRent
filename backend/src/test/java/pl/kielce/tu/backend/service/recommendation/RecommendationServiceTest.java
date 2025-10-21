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
import pl.kielce.tu.backend.mapper.PageMapper;
import pl.kielce.tu.backend.mapper.RecommendationMapper;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.dto.DvdDto;
import pl.kielce.tu.backend.model.dto.PagedResponseDto;
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
    private PageMapper pageMapper;

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
        recommendationService = new RecommendationService(pageMapper, cookieService, userRepository,
                claimsExtractor, userContextLogger, strategies, recommendationMapper);
        ReflectionTestUtils.setField(recommendationService, "jwtSecret", jwtSecret);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldHandleGetDvdRecommendations_successfullyGenerateRecommendations() {
        Long userId = 1L;
        String token = "validToken";
        User user = createTestUser(userId);
        List<Dvd> dvds = createTestDvds();
        List<DvdDto> expectedDtos = createTestDvdDtos();
        PagedResponseDto<DvdDto> pagedResponse = PagedResponseDto.<DvdDto>builder()
                .content(expectedDtos)
                .totalElements(2)
                .totalPages(1)
                .currentPage(0)
                .pageSize(20)
                .build();

        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(token);
        when(claimsExtractor.extractUserId(token, jwtSecret)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(strategy1.getPriority()).thenReturn(1);
        when(strategy2.getPriority()).thenReturn(2);
        when(strategy1.recommend(eq(user), any())).thenReturn(dvds);
        when(strategy2.recommend(eq(user), any())).thenReturn(Collections.emptyList());
        when(strategy1.getReason()).thenReturn("Test reason 1");
        when(recommendationMapper.mapToRecommendationDtos(eq(dvds), anyString(), any())).thenReturn(expectedDtos);
        PagedResponseDto<DvdDto> mockResponse = (PagedResponseDto<DvdDto>) (PagedResponseDto<?>) pagedResponse;
        when(pageMapper.toPagedResponse(any())).thenAnswer(invocation -> mockResponse);

        ResponseEntity<PagedResponseDto<DvdDto>> response = recommendationService.handleGetDvdRecommendations(request,
                0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        PagedResponseDto<DvdDto> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(2, responseBody.getContent().size());
        assertEquals("Test reason 1", responseBody.getContent().get(0).getRecommendationReason());
        verify(userRepository).findById(userId);
        verify(strategy1).recommend(eq(user), any());
    }

    @Test
    void shouldHandleGetDvdRecommendations_returnInternalServerError_whenTokenNotFound() {
        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(null);
        ResponseEntity<PagedResponseDto<DvdDto>> response = recommendationService.handleGetDvdRecommendations(request,
                0, 20);
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

        ResponseEntity<PagedResponseDto<DvdDto>> response = recommendationService.handleGetDvdRecommendations(request,
                0, 20);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userRepository).findById(userId);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGenerateRecommendations_limitToTwentyResults() {
        Long userId = 1L;
        String token = "validToken";
        User user = createTestUser(userId);
        List<Dvd> manyDvds = createManyTestDvds(25);
        List<DvdDto> manyDtos = createManyTestDtos();
        PagedResponseDto<DvdDto> pagedResponse = PagedResponseDto.<DvdDto>builder()
                .content(manyDtos)
                .totalElements(20)
                .totalPages(1)
                .currentPage(0)
                .pageSize(20)
                .build();

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
        PagedResponseDto<DvdDto> mockResponse = (PagedResponseDto<DvdDto>) (PagedResponseDto<?>) pagedResponse;
        when(pageMapper.toPagedResponse(any())).thenAnswer(invocation -> mockResponse);

        ResponseEntity<PagedResponseDto<DvdDto>> response = recommendationService.handleGetDvdRecommendations(request,
                0, 20);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        PagedResponseDto<DvdDto> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(20, responseBody.getContent().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldHandleGetDvdRecommendations_returnEmptyList_whenNoRecommendations() {
        Long userId = 1L;
        String token = "validToken";
        User user = createTestUser(userId);
        PagedResponseDto<DvdDto> emptyResponse = PagedResponseDto.<DvdDto>builder()
                .content(Collections.emptyList())
                .totalElements(0)
                .totalPages(0)
                .currentPage(0)
                .pageSize(20)
                .build();

        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(token);
        when(claimsExtractor.extractUserId(token, jwtSecret)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(strategy1.getPriority()).thenReturn(1);
        when(strategy2.getPriority()).thenReturn(2);
        when(strategy1.recommend(eq(user), any())).thenReturn(Collections.emptyList());
        when(strategy2.recommend(eq(user), any())).thenReturn(Collections.emptyList());
        PagedResponseDto<DvdDto> mockResponse = (PagedResponseDto<DvdDto>) (PagedResponseDto<?>) emptyResponse;
        when(pageMapper.toPagedResponse(any())).thenAnswer(invocation -> mockResponse);

        ResponseEntity<PagedResponseDto<DvdDto>> response = recommendationService.handleGetDvdRecommendations(request,
                0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        PagedResponseDto<DvdDto> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(0, responseBody.getContent().size());
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
