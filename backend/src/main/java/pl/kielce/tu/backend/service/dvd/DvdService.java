package pl.kielce.tu.backend.service.dvd;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.mapper.DvdFilterMapper;
import pl.kielce.tu.backend.mapper.DvdMapper;
import pl.kielce.tu.backend.model.dto.DvdDto;
import pl.kielce.tu.backend.model.dto.DvdFilterDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.repository.DvdRepository;
import pl.kielce.tu.backend.service.dvd.filter.DvdFilterService;
import pl.kielce.tu.backend.service.resource.ResourceService;
import pl.kielce.tu.backend.util.UserContextLogger;

@Service
@RequiredArgsConstructor
public class DvdService {

    private final DvdMapper dvdMapper;
    private final DvdRepository dvdRepository;
    private final DvdUpdateService updateService;
    private final DvdFilterMapper dvdFilterMapper;
    private final ResourceService resourceService;
    private final DvdFilterService dvdFilterService;
    private final UserContextLogger userContextLogger;
    private final DvdValidationService validationService;

    public ResponseEntity<List<DvdDto>> handleGetAllDvdsWithOptionalFilters(String searchPhrase,
            List<String> genreNames, List<Long> genreIds) {
        if (hasAnyFilterParams(searchPhrase, genreNames, genreIds)) {
            return handleGetFilteredDvds(searchPhrase, genreNames, genreIds);
        }
        return handleGetAllDvds();
    }

    public ResponseEntity<List<DvdDto>> handleGetAllDvds() {
        try {
            userContextLogger.logUserOperation("GET_ALL_DVDS", "Fetching all DVDs");
            List<Dvd> dvds = dvdRepository.findAll();
            List<DvdDto> dvdDtos = convertToDtoList(dvds);
            return ResponseEntity.status(HttpStatus.OK).body(dvdDtos);
        } catch (Exception e) {
            userContextLogger.logUserOperation("GET_ALL_DVDS", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<List<DvdDto>> handleGetFilteredDvds(String searchPhrase, List<String> genreNames,
            List<Long> genreIds) {
        try {
            userContextLogger.logUserOperation("GET_FILTERED_DVDS", "Fetching filtered DVDs");
            DvdFilterDto filterDto = dvdFilterMapper.mapToFilterDto(searchPhrase, genreNames, genreIds);
            List<Dvd> dvds = getFilteredDvds(filterDto);
            List<DvdDto> dvdDtos = convertToDtoList(dvds);
            return ResponseEntity.status(HttpStatus.OK).body(dvdDtos);
        } catch (Exception e) {
            userContextLogger.logUserOperation("GET_FILTERED_DVDS", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<DvdDto> handleGetDvdById(String id) {
        try {
            Long dvdId = parseId(id);
            userContextLogger.logUserOperation("GET_DVD_BY_ID", "Fetching DVD with ID: " + dvdId);
            Dvd dvd = getDvdById(dvdId);
            DvdDto enhancedDto = dvdMapper.toEnhancedDto(dvd);
            return ResponseEntity.status(HttpStatus.OK).body(enhancedDto);
        } catch (NumberFormatException e) {
            userContextLogger.logUserOperation("GET_DVD_BY_ID", "Invalid ID format: " + id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            userContextLogger.logUserOperation("GET_DVD_BY_ID", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<Void> handleCreateDvd(DvdDto dvdDto) {
        try {
            userContextLogger.logUserOperation("CREATE_DVD", "Creating DVD: " + dvdDto.getTitle());
            validationService.validateForCreation(dvdDto);
            processPosterImage(dvdDto);
            Dvd dvd = dvdMapper.toDvd(dvdDto);
            dvdRepository.save(dvd);
            userContextLogger.logUserOperation("CREATE_DVD", "Successfully created DVD");
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (ValidationException e) {
            userContextLogger.logUserOperation("CREATE_DVD", "Validation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        } catch (Exception e) {
            userContextLogger.logUserOperation("CREATE_DVD", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<Void> handleUpdateDvd(String id, DvdDto dvdDto) {
        try {
            Long dvdId = parseId(id);
            userContextLogger.logUserOperation("UPDATE_DVD", "Updating DVD with ID: " + dvdId);
            validationService.validateForUpdate(dvdDto);
            Dvd existingDvd = getDvdById(dvdId);
            updateService.applyUpdates(existingDvd, dvdDto);
            dvdRepository.save(existingDvd);
            userContextLogger.logUserOperation("UPDATE_DVD", "Successfully updated DVD");
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private List<DvdDto> convertToDtoList(List<Dvd> dvds) {
        return dvds.stream()
                .map(dvdMapper::toDto)
                .toList();
    }

    private Long parseId(String id) {
        return Long.parseLong(id);
    }

    private Dvd getDvdById(Long dvdId) {
        return dvdRepository.findById(dvdId)
                .orElseThrow(() -> new EntityNotFoundException("DVD not found with id: " + dvdId));
    }

    private void processPosterImage(DvdDto dvdDto) throws ValidationException {
        if (dvdDto.getPosterImage() != null && !dvdDto.getPosterImage().trim().isEmpty()) {
            String savedFilename = resourceService.savePosterImage(dvdDto.getPosterImage());
            String posterUrl = resourceService.generatePosterUrl(savedFilename);
            dvdDto.setPosterUrl(posterUrl);
        }
    }

    private boolean hasAnyFilterParams(String searchPhrase, List<String> genreNames, List<Long> genreIds) {
        return (searchPhrase != null && !searchPhrase.trim().isEmpty()) ||
                (genreNames != null && !genreNames.isEmpty()) ||
                (genreIds != null && !genreIds.isEmpty());
    }

    private List<Dvd> getFilteredDvds(DvdFilterDto filterDto) {
        List<Dvd> allDvds = dvdRepository.findAll();
        if (dvdFilterMapper.hasAnyFilter(filterDto)) {
            return dvdFilterService.applyFilters(allDvds, filterDto);
        }
        return allDvds;
    }

}
