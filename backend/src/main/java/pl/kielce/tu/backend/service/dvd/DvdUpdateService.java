package pl.kielce.tu.backend.service.dvd;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.mapper.DvdMapper;
import pl.kielce.tu.backend.model.dto.DvdDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.service.resource.ResourceService;

@Component
@RequiredArgsConstructor
public class DvdUpdateService {

    private final DvdMapper dvdMapper;
    private final ResourceService resourceService;
    private final DvdValidationService validationService;

    public void applyUpdates(Dvd dvd, DvdDto dvdDto) throws ValidationException {
        updateBasicFields(dvd, dvdDto);
        updateComplexFields(dvd, dvdDto);
        updatePosterIfPresent(dvd, dvdDto);
    }

    private void updateBasicFields(Dvd dvd, DvdDto dvdDto) throws ValidationException {
        updateTitle(dvd, dvdDto);
        updateYear(dvd, dvdDto);
        updateDescription(dvd, dvdDto);
        updateDuration(dvd, dvdDto);
        updateCopies(dvd, dvdDto);
        updatePrice(dvd, dvdDto);
        updateAvailability(dvd, dvdDto);
    }

    private void updateComplexFields(Dvd dvd, DvdDto dvdDto) throws ValidationException {
        updateGenres(dvd, dvdDto);
        updateDirectors(dvd, dvdDto);
    }

    private void updateTitle(Dvd dvd, DvdDto dvdDto) throws ValidationException {
        if (dvdDto.getTitle() != null) {
            validationService.validateTitle(dvdDto.getTitle());
            dvd.setTitle(dvdDto.getTitle());
        }
    }

    private void updateYear(Dvd dvd, DvdDto dvdDto) throws ValidationException {
        if (dvdDto.getReleaseYear() != null) {
            validationService.validateYear(dvdDto.getReleaseYear());
            dvd.setReleaseYear(dvdDto.getReleaseYear());
        }
    }

    private void updateDescription(Dvd dvd, DvdDto dvdDto) throws ValidationException {
        if (dvdDto.getDescription() != null) {
            validationService.validateDescription(dvdDto.getDescription());
            dvd.setDescription(dvdDto.getDescription());
        }
    }

    private void updateDuration(Dvd dvd, DvdDto dvdDto) throws ValidationException {
        if (dvdDto.getDurationMinutes() != null) {
            validationService.validateDuration(dvdDto.getDurationMinutes());
            dvd.setDurationMinutes(dvdDto.getDurationMinutes());
        }
    }

    private void updateCopies(Dvd dvd, DvdDto dvdDto) throws ValidationException {
        if (dvdDto.getCopiesAvailable() != null) {
            validationService.validateCopies(dvdDto.getCopiesAvailable());
            dvd.setCopiesAvalaible(dvdDto.getCopiesAvailable());
        }
    }

    private void updatePrice(Dvd dvd, DvdDto dvdDto) throws ValidationException {
        if (dvdDto.getRentalPricePerDay() != null) {
            validationService.validatePrice(dvdDto.getRentalPricePerDay());
            dvd.setRentalPricePerDay(dvdDto.getRentalPricePerDay());
        }
    }

    private void updateAvailability(Dvd dvd, DvdDto dvdDto) {
        if (dvdDto.getAvailable() != null) {
            dvd.setAvalaible(dvdDto.getAvailable());
        }
    }

    private void updateGenres(Dvd dvd, DvdDto dvdDto) throws ValidationException {
        if (dvdDto.getGenresIdentifiers() != null) {
            validationService.validateGenres(dvdDto.getGenresIdentifiers());
            Dvd tempDvd = createTempDvdWithGenres(dvdDto);
            dvd.setGenres(tempDvd.getGenres());
        }
    }

    private void updateDirectors(Dvd dvd, DvdDto dvdDto) throws ValidationException {
        if (dvdDto.getDirectors() != null) {
            validationService.validateDirectors(dvdDto.getDirectors());
            dvd.setDirectors(dvdDto.getDirectors());
        }
    }

    private void updatePosterIfPresent(Dvd dvd, DvdDto dvdDto) throws ValidationException {
        if (hasPosterImage(dvdDto)) {
            String posterUrl = processPosterImage(dvdDto);
            dvd.setPosterUrl(posterUrl);
        }
    }

    private Dvd createTempDvdWithGenres(DvdDto dvdDto) {
        DvdDto tempDto = DvdDto.builder()
                .genresIdentifiers(dvdDto.getGenresIdentifiers())
                .build();
        return dvdMapper.toDvd(tempDto);
    }

    private boolean hasPosterImage(DvdDto dvdDto) {
        return dvdDto.getPosterImage() != null && !dvdDto.getPosterImage().trim().isEmpty();
    }

    private String processPosterImage(DvdDto dvdDto) throws ValidationException {
        String savedFilename = resourceService.savePosterImage(dvdDto.getPosterImage());
        return resourceService.generatePosterUrl(savedFilename);
    }
    
}
