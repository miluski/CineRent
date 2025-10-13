package pl.kielce.tu.backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.model.dto.DvdDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class RecommendationMapperTest {

    @Mock
    private DvdMapper dvdMapper;

    @Mock
    private UserContextLogger logger;

    @InjectMocks
    private RecommendationMapper recommendationMapper;

    @Test
    void returnsEmptyListWhenInputIsNull() {
        List<DvdDto> result = recommendationMapper.mapToRecommendationDtos(null, "reason", logger);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyListWhenInputIsEmpty() {
        List<DvdDto> result = recommendationMapper.mapToRecommendationDtos(Collections.emptyList(), "reason", logger);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void mapsDvdsAndSetsRecommendationReason() {
        Dvd dvd = new Dvd();
        DvdDto dto = new DvdDto();
        when(dvdMapper.toEnhancedDto(dvd)).thenReturn(dto);

        List<DvdDto> result = recommendationMapper.mapToRecommendationDtos(Arrays.asList(dvd), "because you liked X",
                logger);

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
        assertEquals("because you liked X", result.get(0).getRecommendationReason());
    }

    @Test
    void filtersOutNullDvdAndNullDto() {
        Dvd validDvd = new Dvd();
        validDvd.setId(1L);
        Dvd nullReturningDvd = new Dvd();
        nullReturningDvd.setId(2L);

        DvdDto validDto = new DvdDto();
        when(dvdMapper.toEnhancedDto(validDvd)).thenReturn(validDto);
        when(dvdMapper.toEnhancedDto(nullReturningDvd)).thenReturn(null);

        List<Dvd> input = Arrays.asList(null, nullReturningDvd, validDvd);
        List<DvdDto> result = recommendationMapper.mapToRecommendationDtos(input, "r", logger);

        assertEquals(1, result.size());
        assertEquals("r", result.get(0).getRecommendationReason());
    }

    @Test
    void logsErrorWhenMappingThrowsException() {
        Dvd badDvd = new Dvd();
        when(dvdMapper.toEnhancedDto(badDvd)).thenThrow(new RuntimeException("mapping failed"));

        List<DvdDto> result = recommendationMapper.mapToRecommendationDtos(Arrays.asList(badDvd), "r", logger);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(logger).logUserOperation(eq("DVD_MAPPING_ERROR"), contains("mapping failed"));
    }
}
