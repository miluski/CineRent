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
    @Operation(summary = "Get all genres", description = """
            Retrieves a list of all available genres in the system. \
            Returns genre information including ID and name, which can be used \
            for filtering DVDs or setting user preferences.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all genres", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(example = """
                    [
                      {
                        "id": 1,
                        "name": "Science Fiction"
                      },
                      {
                        "id": 2,
                        "name": "Action"
                      },
                      {
                        "id": 3,
                        "name": "Drama"
                      }
                    ]""")))),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred while retrieving genres", content = @Content)
    })
    public ResponseEntity<List<GenreDto>> getGenres() {
        return genreService.handleGetGenres();
    }

    @PostMapping("create")
    @Operation(summary = "Create a new genre (Admin only)", description = """
            Creates a new genre with the provided name. The genre name must be unique \
            and between 5-75 characters. This is typically used by administrators \
            to add new movie categories to the system.""", security = {
            @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "accessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Genre created successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content),
            @ApiResponse(responseCode = "422", description = "Validation failed - genre name too short/long or already exists", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred while creating genre", content = @Content)
    })
    public ResponseEntity<Void> createGenre(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Genre creation data", required = true, content = @Content(schema = @Schema(example = """
                    {
                      "name": "Science Fiction"
                    }"""))) @Valid @RequestBody(required = true) GenreDto genreDto) {
        return genreService.handleCreateGenre(genreDto);
    }

    @DeleteMapping("{id}/delete")
    @Operation(summary = "Delete a genre (Admin only)", description = """
            Deletes a genre from the system if certain conditions are met: \
            - The system must retain at least 2 genres after deletion \
            - The genre must not be assigned to any DVDs in the system \
            - The genre must exist in the system \
            This operation is typically restricted to administrators.""", security = {
            @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "accessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Genre deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid genre ID format or business rule validation failed (minimum genre constraint violated or genre assigned to DVDs)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content),
            @ApiResponse(responseCode = "404", description = "Genre not found with the specified ID", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred while deleting genre", content = @Content)
    })
    public ResponseEntity<Void> deleteGenre(
            @Parameter(description = "Unique identifier of the genre to delete", example = "1") @PathVariable String id) {
        return genreService.handleDeleteGenre(id);
    }

}
