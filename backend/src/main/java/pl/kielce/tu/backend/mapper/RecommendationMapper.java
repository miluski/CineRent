package pl.kielce.tu.backend.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.dto.DvdDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class RecommendationMapper {

    private final DvdMapper dvdMapper;

    public List<DvdDto> mapToRecommendationDtos(List<Dvd> dvds, String reason, UserContextLogger logger) {
        if (dvds == null || dvds.isEmpty()) {
            return new ArrayList<>();
        }

        return dvds.stream()
                .filter(this::isValidDvd)
                .map(dvd -> mapDvdToRecommendationDto(dvd, reason, logger))
                .filter(this::isValidDto)
                .collect(Collectors.toList());
    }

    private boolean isValidDvd(Dvd dvd) {
        return dvd != null;
    }

    private DvdDto mapDvdToRecommendationDto(Dvd dvd, String reason, UserContextLogger logger) {
        try {
            DvdDto dto = dvdMapper.toEnhancedDto(dvd);
            if (dto != null) {
                dto.setRecommendationReason(reason);
            }
            return dto;
        } catch (Exception e) {
            logMappingError(e, logger);
            return null;
        }
    }

    private void logMappingError(Exception e, UserContextLogger logger) {
        if (logger != null) {
            logger.logUserOperation("DVD_MAPPING_ERROR",
                    "Error mapping DVD to recommendation DTO: " + e.getMessage());
        }
    }

    private boolean isValidDto(DvdDto dto) {
        return dto != null;
    }

}
