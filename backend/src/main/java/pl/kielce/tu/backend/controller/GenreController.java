package pl.kielce.tu.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.dto.GenreDto;
import pl.kielce.tu.backend.service.genre.GenreService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/genres")
@Tag(name = "Genre Management", description = "Operations for managing Genres in the rental system")
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    @Operation(summary = "Get all genres", description = "Retrieves a list of all available genres in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all genres", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = GenreDto.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred while retrieving genres")
    })
    public ResponseEntity<List<GenreDto>> getGenres() {
        return genreService.handleGetGenres();
    }

    @PostMapping("create")
    @Operation(summary = "Create a new genre", description = "Creates a new genre with unique name between 5-75 characters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Genre created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid genre data provided or genre name already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred while creating genre")
    })
    public ResponseEntity<Void> createGenre(@Valid @RequestBody GenreDto genreDto) {
        return genreService.handleCreateGenre(genreDto);
    }

    @DeleteMapping("{id}/delete")
    @Operation(summary = "Delete a genre", description = "Deletes a genre if minimum 2 genres remain and genre is not assigned to DVDs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Genre deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid genre ID, genre not found, minimum genre constraint violated, or genre assigned to DVDs"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred while deleting genre")
    })
    public ResponseEntity<Void> deleteGenre(
            @Parameter(description = "Unique identifier of the genre to delete", example = "1") @PathVariable String id) {
        return genreService.handleDeleteGenre(id);
    }

}
