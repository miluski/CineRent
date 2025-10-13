package pl.kielce.tu.backend.service.genre;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.persistence.EntityNotFoundException;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.mapper.GenreMapper;
import pl.kielce.tu.backend.model.dto.GenreDto;
import pl.kielce.tu.backend.model.entity.Genre;
import pl.kielce.tu.backend.repository.GenreRepository;
import pl.kielce.tu.backend.service.user.UserGenreService;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock
    private GenreMapper genreMapper;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private UserGenreService userGenreService;

    @Mock
    private GenreValidationService validationService;

    @Mock
    private UserContextLogger userContextLogger;

    private GenreService service;

    @BeforeEach
    void setUp() {
        service = new GenreService(genreMapper, genreRepository, userGenreService, userContextLogger,
                validationService);
    }

    @Test
    void handleGetGenres_success_returnsOkWithGenres() {
        Genre genre1 = Genre.builder().id(1L).name("Action").build();
        Genre genre2 = Genre.builder().id(2L).name("Comedy").build();
        GenreDto dto1 = GenreDto.builder().id(1L).name("Action").build();
        GenreDto dto2 = GenreDto.builder().id(2L).name("Comedy").build();

        when(genreRepository.findAll()).thenReturn(Arrays.asList(genre1, genre2));
        when(genreMapper.toDto(genre1)).thenReturn(dto1);
        when(genreMapper.toDto(genre2)).thenReturn(dto2);

        ResponseEntity<List<GenreDto>> response = service.handleGetGenres();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<GenreDto> genres = response.getBody();
        assertNotNull(genres);
        assertEquals(2, genres.size());
        assertEquals("Action", genres.get(0).getName());
        assertEquals("Comedy", genres.get(1).getName());
    }

    @Test
    void handleGetGenres_exception_returnsInternalServerError() {
        when(genreRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<List<GenreDto>> response = service.handleGetGenres();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userContextLogger).logEndpointAccess("GET", "/api/v1/genres", "ERROR: Database error");
    }

    @Test
    void handleCreateGenre_success_returnsCreated() throws ValidationException {
        GenreDto genreDto = GenreDto.builder().name("Action").build();
        Genre genre = Genre.builder().name("Action").build();

        doNothing().when(validationService).validateForCreation(genreDto);
        when(genreMapper.toGenre(genreDto)).thenReturn(genre);
        when(genreRepository.save(genre)).thenReturn(genre);

        ResponseEntity<Void> response = service.handleCreateGenre(genreDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(genreRepository).save(genre);
        verify(userContextLogger).logEndpointAccess("POST", "/api/v1/genres/create", "SUCCESS");
    }

    @Test
    void handleCreateGenre_validationException_returnsBadRequest() throws ValidationException {
        GenreDto genreDto = GenreDto.builder().name("Action").build();

        doThrow(new ValidationException("Genre name already exists"))
                .when(validationService).validateForCreation(genreDto);

        ResponseEntity<Void> response = service.handleCreateGenre(genreDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(genreRepository, never()).save(any());
        verify(userContextLogger).logEndpointAccess("POST", "/api/v1/genres/create",
                "VALIDATION_ERROR: Genre name already exists");
    }

    @Test
    void handleCreateGenre_exception_returnsInternalServerError() throws ValidationException {
        GenreDto genreDto = GenreDto.builder().name("Action").build();

        doNothing().when(validationService).validateForCreation(genreDto);
        when(genreMapper.toGenre(genreDto)).thenThrow(new RuntimeException("Mapper error"));

        ResponseEntity<Void> response = service.handleCreateGenre(genreDto);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userContextLogger).logEndpointAccess("POST", "/api/v1/genres/create", "ERROR: Mapper error");
    }

    @Test
    void handleDeleteGenre_success_returnsNoContent() throws ValidationException {
        String genreId = "1";

        doNothing().when(validationService).validateForDeletion(genreId);
        doNothing().when(userGenreService).removeGenreFromAllUsers(1L);

        ResponseEntity<Void> response = service.handleDeleteGenre(genreId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userGenreService).removeGenreFromAllUsers(1L);
        verify(genreRepository).deleteById(1L);
        verify(userContextLogger).logEndpointAccess("DELETE", "/api/v1/genres/1/delete", "SUCCESS");
    }

    @Test
    void handleDeleteGenre_validationException_returnsBadRequest() throws ValidationException {
        String genreId = "1";

        doThrow(new ValidationException("Cannot delete genre: minimum 2 genres must remain"))
                .when(validationService).validateForDeletion(genreId);

        ResponseEntity<Void> response = service.handleDeleteGenre(genreId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(genreRepository, never()).deleteById(any());
        verify(userContextLogger).logEndpointAccess("DELETE", "/api/v1/genres/1/delete",
                "VALIDATION_ERROR: Cannot delete genre: minimum 2 genres must remain");
    }

    @Test
    void handleDeleteGenre_entityNotFoundException_returnsNotFound() throws ValidationException {
        String genreId = "999";

        doThrow(new EntityNotFoundException("Genre not found with ID: 999"))
                .when(validationService).validateForDeletion(genreId);

        ResponseEntity<Void> response = service.handleDeleteGenre(genreId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(genreRepository, never()).deleteById(any());
        verify(userContextLogger).logEndpointAccess("DELETE", "/api/v1/genres/999/delete",
                "NOT_FOUND: Genre not found with ID: 999");
    }

    @Test
    void handleDeleteGenre_exception_returnsInternalServerError() throws ValidationException {
        String genreId = "1";

        doNothing().when(validationService).validateForDeletion(genreId);
        doNothing().when(userGenreService).removeGenreFromAllUsers(1L);
        doThrow(new RuntimeException("Database error")).when(genreRepository).deleteById(1L);

        ResponseEntity<Void> response = service.handleDeleteGenre(genreId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userGenreService).removeGenreFromAllUsers(1L);
        verify(userContextLogger).logEndpointAccess("DELETE", "/api/v1/genres/1/delete", "ERROR: Database error");
    }

    @Test
    void handleDeleteGenre_userGenreServiceException_returnsInternalServerError() throws ValidationException {
        String genreId = "1";

        doNothing().when(validationService).validateForDeletion(genreId);
        doThrow(new RuntimeException("User update error")).when(userGenreService).removeGenreFromAllUsers(1L);

        ResponseEntity<Void> response = service.handleDeleteGenre(genreId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userGenreService).removeGenreFromAllUsers(1L);
        verify(genreRepository, never()).deleteById(any());
        verify(userContextLogger).logEndpointAccess("DELETE", "/api/v1/genres/1/delete", "ERROR: User update error");
    }

}
