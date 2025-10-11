package pl.kielce.tu.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.dto.DvdDto;
import pl.kielce.tu.backend.service.dvd.DvdService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dvd")
@Tag(name = "DVD Management", description = "Operations for managing DVDs in the rental system")
public class DvdController {

    private final DvdService dvdService;

    @GetMapping
    @Operation(summary = "Get all DVDs", description = """
            Retrieves a simplified list of all DVDs available in the rental system. \
            Returns basic information about each DVD including id, title, genres, and availability status. \
            This is used for browsing the DVD collection.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of DVDs retrieved successfully", content = @Content(schema = @Schema(example = """
                    [
                      {
                        "id": 120,
                        "title": "Incepcja",
                        "genres": ["Sci-Fi", "Action"],
                        "status": "AVALAIBLE"
                      },
                      {
                        "id": 124,
                        "title": "Powrót do przyszłości",
                        "genres": ["Sci-Fi"],
                        "status": "AVALAIBLE"
                      }
                    ]"""))),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred while retrieving DVDs", content = @Content)
    })
    public ResponseEntity<List<DvdDto>> getAllDvds() {
        return dvdService.handleGetAllDvds();
    }

    @GetMapping("{id}")
    @Operation(summary = "Get DVD by ID", description = """
            Retrieves detailed information about a specific DVD by its ID. \
            Returns complete DVD data including all metadata, availability status, \
            and rental information. The DVD must exist in the system.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DVD retrieved successfully", content = @Content(schema = @Schema(example = """
                    {
                      "id": 101,
                      "title": "Matrix",
                      "genres": ["Sci-Fi", "Action"],
                      "releaseYear": 1999,
                      "directors": ["Lana Wachowski", "Lilly Wachowski"],
                      "description": "Neo odkrywa prawdę o rzeczywistości i staje do walki z systemem, który kontroluje ludzi.",
                      "durationMinutes": 136,
                      "available": true,
                      "copiesAvailable": 5,
                      "rentalPricePerDay": 4.00,
                      "posterUrl": "https://api.example.com/images/dvds/matrix.jpg",
                      "addedAt": "2025-09-01T14:00:00Z"
                    }"""))),
            @ApiResponse(responseCode = "404", description = "DVD not found with the specified ID", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid DVD ID format", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred while retrieving DVD", content = @Content)
    })
    public ResponseEntity<DvdDto> getEnhancedDvd(@PathVariable String id) {
        return dvdService.handleGetDvdById(id);
    }

    @PostMapping("/create")
    @Operation(summary = "Create a new DVD", description = """
            Creates a new DVD entry in the rental system with the provided information. \
            Required fields: title, at least one genre identifier that exists in the database, \
            release year, at least one director, description, duration in minutes, \
            number of available copies, and rental price per day. \
            Optional: poster image as base64 encoded string. The genres must exist in the system.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "DVD successfully created"),
            @ApiResponse(responseCode = "422", description = "Validation failed - invalid DVD data (title too short/long, invalid release year, empty directors list, invalid description length, invalid duration, negative copies, invalid price, or non-existent genre identifiers)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred during DVD creation", content = @Content)
    })
    public ResponseEntity<Void> createDvd(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "DVD creation data", required = true, content = @Content(schema = @Schema(example = """
                    {
                      "title": "Matrix",
                      "genresIdentifiers": [1, 2],
                      "releaseYear": 1999,
                      "directors": ["Lana Wachowski", "Lilly Wachowski"],
                      "description": "Neo odkrywa prawdę o rzeczywistości i staje do walki z systemem.",
                      "durationMinutes": 136,
                      "available": true,
                      "copiesAvailable": 5,
                      "rentalPricePerDay": 4.00,
                      "posterImage": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD..."
                    }"""))) @Valid @RequestBody DvdDto dvdDto) {
        return dvdService.handleCreateDvd(dvdDto);
    }

    @PatchMapping("{id}/edit")
    @Operation(summary = "Update DVD information", description = """
            Partially updates an existing DVD's information. The DVD must exist in the system. \
            All fields are optional but at least one field must be provided for the update. \
            Field validations are the same as for creation. Poster image can be provided \
            as base64 encoded string.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "DVD successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid DVD ID format", content = @Content),
            @ApiResponse(responseCode = "404", description = "DVD not found with the specified ID", content = @Content),
            @ApiResponse(responseCode = "422", description = "Validation error - invalid field value, no fields provided, or non-existent genre identifiers", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred during DVD update", content = @Content)
    })
    public ResponseEntity<Void> editDvd(
            @PathVariable String id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Partial DVD update data. At least one field must be provided.", required = true, content = @Content(schema = @Schema(example = """
                    {
                      "title": "Matrix",
                      "genresIdentifiers": [1, 3],
                      "releaseYear": 2001,
                      "directors": ["Jan Wachowski", "Lilly Wachowski"],
                      "description": "Nao odkrywa prawdę o rzeczywistości i staje do walki z systemem.",
                      "durationMinutes": 138,
                      "available": true,
                      "copiesAvailable": 5,
                      "rentalPricePerDay": 5.00,
                      "posterImage": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD..."
                    }"""))) @Valid @RequestBody DvdDto dvdDto) {
        return dvdService.handleUpdateDvd(id, dvdDto);
    }

}
