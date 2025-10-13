package pl.kielce.tu.backend.service.genre;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.mapper.GenreMapper;
import pl.kielce.tu.backend.model.dto.GenreDto;
import pl.kielce.tu.backend.model.entity.Genre;
import pl.kielce.tu.backend.repository.GenreRepository;
import pl.kielce.tu.backend.service.user.UserGenreService;
import pl.kielce.tu.backend.util.UserContextLogger;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreMapper genreMapper;
    private final GenreRepository genreRepository;
    private final UserGenreService userGenreService;
    private final UserContextLogger userContextLogger;
    private final GenreValidationService validationService;

    public ResponseEntity<List<GenreDto>> handleGetGenres() {
        try {
            userContextLogger.logEndpointAccess("GET", "/api/v1/genres", "STARTED");
            List<GenreDto> genres = getAllGenres();
            userContextLogger.logEndpointAccess("GET", "/api/v1/genres", "SUCCESS");
            return ResponseEntity.status(HttpStatus.OK).body(genres);
        } catch (Exception e) {
            userContextLogger.logEndpointAccess("GET", "/api/v1/genres", "ERROR: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<Void> handleCreateGenre(GenreDto genreDto) {
        try {
            userContextLogger.logEndpointAccess("POST", "/api/v1/genres/create", "STARTED");
            validationService.validateForCreation(genreDto);
            createGenre(genreDto);
            userContextLogger.logEndpointAccess("POST", "/api/v1/genres/create", "SUCCESS");
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (ValidationException e) {
            userContextLogger.logEndpointAccess("POST", "/api/v1/genres/create", "VALIDATION_ERROR: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            userContextLogger.logEndpointAccess("POST", "/api/v1/genres/create", "ERROR: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<Void> handleDeleteGenre(String id) {
        try {
            userContextLogger.logEndpointAccess("DELETE", "/api/v1/genres/" + id + "/delete", "STARTED");
            validationService.validateForDeletion(id);
            deleteGenre(Long.parseLong(id));
            userContextLogger.logEndpointAccess("DELETE", "/api/v1/genres/" + id + "/delete", "SUCCESS");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (EntityNotFoundException e) {
            userContextLogger.logEndpointAccess("DELETE", "/api/v1/genres/" + id + "/delete",
                    "NOT_FOUND: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ValidationException e) {
            userContextLogger.logEndpointAccess("DELETE", "/api/v1/genres/" + id + "/delete",
                    "VALIDATION_ERROR: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            userContextLogger.logEndpointAccess("DELETE", "/api/v1/genres/" + id + "/delete",
                    "ERROR: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private List<GenreDto> getAllGenres() {
        return genreRepository.findAll()
                .stream()
                .map(genreMapper::toDto)
                .collect(Collectors.toList());
    }

    private void createGenre(GenreDto genreDto) {
        Genre genre = genreMapper.toGenre(genreDto);
        genreRepository.save(genre);
    }

    @Transactional
    private void deleteGenre(Long genreId) {
        userGenreService.removeGenreFromAllUsers(genreId);
        genreRepository.deleteById(genreId);
    }

}
