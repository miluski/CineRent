package pl.kielce.tu.backend.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import pl.kielce.tu.backend.model.dto.GenreDto;
import pl.kielce.tu.backend.service.genre.GenreService;


@ExtendWith(MockitoExtension.class)
class GenreControllerTest {

    @Mock
    private GenreService genreService;

    private GenreController controller;

    @BeforeEach
    void setUp() {
        controller = new GenreController(genreService);
    }

    @Test
    void getGenres_delegatesToService_andReturnsResponse() {
        List<GenreDto> list = List.of(org.mockito.Mockito.mock(GenreDto.class));
        ResponseEntity<List<GenreDto>> expected = ResponseEntity.ok(list);

        when(genreService.handleGetGenres()).thenReturn(expected);

        ResponseEntity<List<GenreDto>> actual = controller.getGenres();

        assertSame(expected, actual);
        verify(genreService).handleGetGenres();
    }

    @Test
    void createGenre_delegatesToService_andReturnsResponse() {
        GenreDto dto = org.mockito.Mockito.mock(GenreDto.class);
        ResponseEntity<Void> expected = ResponseEntity.status(HttpStatus.CREATED).build();

        when(genreService.handleCreateGenre(dto)).thenReturn(expected);

        ResponseEntity<Void> actual = controller.createGenre(dto);

        assertSame(expected, actual);
        verify(genreService).handleCreateGenre(dto);
    }

    @Test
    void deleteGenre_delegatesToService_andReturnsResponse() {
        String id = "1";
        ResponseEntity<Void> expected = ResponseEntity.noContent().build();

        when(genreService.handleDeleteGenre(id)).thenReturn(expected);

        ResponseEntity<Void> actual = controller.deleteGenre(id);

        assertSame(expected, actual);
        verify(genreService).handleDeleteGenre(id);
    }
}
