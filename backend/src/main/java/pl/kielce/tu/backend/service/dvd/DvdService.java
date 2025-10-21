package pl.kielce.tu.backend.service.dvd;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.mapper.DvdFilterMapper;
import pl.kielce.tu.backend.mapper.DvdMapper;
import pl.kielce.tu.backend.mapper.PageMapper;
import pl.kielce.tu.backend.model.dto.DvdDto;
import pl.kielce.tu.backend.model.dto.DvdFilterDto;
import pl.kielce.tu.backend.model.dto.PagedResponseDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.repository.DvdRepository;
import pl.kielce.tu.backend.repository.specification.DvdSpecification;
import pl.kielce.tu.backend.service.resource.ResourceService;
import pl.kielce.tu.backend.util.UserContextLogger;

@Service
@RequiredArgsConstructor
public class DvdService {

    private final DvdMapper dvdMapper;
    private final PageMapper pageMapper;
    private final DvdRepository dvdRepository;
    private final DvdUpdateService updateService;
    private final DvdFilterMapper dvdFilterMapper;
    private final ResourceService resourceService;
    private final DvdSpecification dvdSpecification;
    private final UserContextLogger userContextLogger;
    private final DvdValidationService validationService;

    public ResponseEntity<PagedResponseDto<DvdDto>> handleGetAllDvdsWithOptionalFilters(String searchPhrase,
            List<String> genreNames, List<Long> genreIds, int page, int size) {
        if (hasAnyFilterParams(searchPhrase, genreNames, genreIds)) {
            return handleGetFilteredDvds(searchPhrase, genreNames, genreIds, page, size);
        }
        return handleGetAllDvds(page, size);
    }

    public ResponseEntity<PagedResponseDto<DvdDto>> handleGetAllDvds(int page, int size) {
        try {
            userContextLogger.logUserOperation("GET_ALL_DVDS", "Fetching all DVDs");
            Pageable pageable = createPageable(page, size);
            Page<Dvd> dvdPage = dvdRepository.findAll(pageable);
            PagedResponseDto<DvdDto> response = pageMapper.toPagedResponse(dvdPage, dvdMapper::toDto);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            userContextLogger.logUserOperation("GET_ALL_DVDS", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<PagedResponseDto<DvdDto>> handleGetFilteredDvds(String searchPhrase, List<String> genreNames,
            List<Long> genreIds, int page, int size) {
        try {
            userContextLogger.logUserOperation("GET_FILTERED_DVDS", "Fetching filtered DVDs");
            DvdFilterDto filterDto = dvdFilterMapper.mapToFilterDto(searchPhrase, genreNames, genreIds);
            Pageable pageable = createPageable(page, size);
            Page<Dvd> dvdPage = getFilteredDvds(filterDto, pageable);
            PagedResponseDto<DvdDto> response = pageMapper.toPagedResponse(dvdPage, dvdMapper::toDto);
            return ResponseEntity.status(HttpStatus.OK).body(response);
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

    private Pageable createPageable(int page, int size) {
        int validatedSize = Math.min(size, 20);
        return PageRequest.of(page, validatedSize);
    }

    private Page<Dvd> getFilteredDvds(DvdFilterDto filterDto, Pageable pageable) {
        Specification<Dvd> specification = dvdSpecification.withFilters(filterDto);
        return dvdRepository.findAll(specification, pageable);
    }

}
