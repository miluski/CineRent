package pl.kielce.tu.backend.mapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.constant.DvdStatuses;
import pl.kielce.tu.backend.model.dto.DvdDto;
import pl.kielce.tu.backend.model.entity.Dvd;

@Component
@RequiredArgsConstructor
public class DvdMapper {

    private final GenreMapper genreMappingService;

    public DvdDto toDto(Dvd dvd) {
        if (dvd == null) {
            return null;
        }

        return DvdDto.builder()
                .id(dvd.getId())
                .title(dvd.getTitle())
                .genres(genreMappingService.mapGenresToNames(dvd.getGenres()))
                .status(determineStatus(dvd))
                .build();
    }

    public DvdDto toEnhancedDto(Dvd dvd) {
        if (dvd == null) {
            return null;
        }

        return DvdDto.builder()
                .id(dvd.getId())
                .title(dvd.getTitle())
                .genres(genreMappingService.mapGenresToNames(dvd.getGenres()))
                .releaseYear(dvd.getReleaseYear())
                .directors(dvd.getDirectors())
                .description(dvd.getDescription())
                .durationMinutes(dvd.getDurationMinutes())
                .available(mapAvailability(dvd))
                .copiesAvailable(mapCopiesAvailable(dvd))
                .rentalPricePerDay(dvd.getRentalPricePerDay())
                .posterUrl(dvd.getPosterUrl())
                .addedAt(dvd.getAddedAt())
                .build();
    }

    public Dvd toDvd(DvdDto dvdDto) {
        if (dvdDto == null) {
            return null;
        }

        return Dvd.builder()
                .id(dvdDto.getId())
                .title(dvdDto.getTitle())
                .genres(genreMappingService.mapGenreIdsToGenres(dvdDto.getGenresIdentifiers()))
                .releaseYear(dvdDto.getReleaseYear())
                .directors(dvdDto.getDirectors())
                .description(dvdDto.getDescription())
                .durationMinutes(dvdDto.getDurationMinutes())
                .avalaible(mapAvailabilityToEntity(dvdDto))
                .copiesAvalaible(mapCopiesAvailableToEntity(dvdDto))
                .rentalPricePerDay(dvdDto.getRentalPricePerDay())
                .posterUrl(dvdDto.getPosterUrl())
                .addedAt(dvdDto.getAddedAt())
                .build();
    }

    private String determineStatus(Dvd dvd) {
        if (dvd.getAvalaible() && dvd.getCopiesAvalaible() > 0) {
            return DvdStatuses.AVALAIBLE.getValue();
        }
        return DvdStatuses.UNAVALAIBLE.getValue();
    }

    private Boolean mapAvailability(Dvd dvd) {
        return dvd.getAvalaible();
    }

    private Integer mapCopiesAvailable(Dvd dvd) {
        return dvd.getCopiesAvalaible();
    }

    private Boolean mapAvailabilityToEntity(DvdDto dvdDto) {
        return dvdDto.getAvailable();
    }

    private Integer mapCopiesAvailableToEntity(DvdDto dvdDto) {
        return dvdDto.getCopiesAvailable();
    }

}
