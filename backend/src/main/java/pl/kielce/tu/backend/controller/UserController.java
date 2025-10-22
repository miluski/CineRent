package pl.kielce.tu.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.dto.DvdDto;
import pl.kielce.tu.backend.model.dto.PagedResponseDto;
import pl.kielce.tu.backend.model.dto.UserDto;
import pl.kielce.tu.backend.service.recommendation.RecommendationService;
import pl.kielce.tu.backend.service.user.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "User", description = "User management operations")
public class UserController {

    private final UserService userService;
    private final RecommendationService recommendationService;

    @Operation(summary = "Get authenticated user data", description = "Retrieves the authenticated user's data including nickname, age, and preferred genre names. User ID is extracted from the JWT token.", security = {
            @SecurityRequirement(name = "accessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User data retrieved successfully", content = @Content(schema = @Schema(example = """
                    {
                      "nickname": "FilmLover99",
                      "age": 24,
                      "preferredGenres": ["Science-Fiction", "Akcja", "Dramat"]
                    }"""))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    @GetMapping
    public ResponseEntity<UserDto> getUser(HttpServletRequest httpServletRequest) {
        return userService.handleGetUser(httpServletRequest);
    }

    @Operation(summary = "Get DVD recommendations with pagination", description = """
            Get personalized DVD recommendations based on user's rental history, age group preferences, and preferred genres. \
            Results are paginated with a maximum of 20 elements per page. Each recommendation strategy contributes up to 20 DVDs.""", security = {
            @SecurityRequirement(name = "accessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recommendations retrieved successfully", content = @Content(schema = @Schema(example = """
                    {
                      "content": [
                        {
                          "id": 120,
                          "title": "Incepcja",
                          "genres": ["Sci-Fi", "Action"],
                          "posterUrl": "http://example.com/posters/inception.jpg",
                          "rentalPricePerDay": 5.99,
                          "availabilityStatus": "AVAILABLE"
                        }
                      ],
                      "totalElements": 45,
                      "totalPages": 3,
                      "currentPage": 0,
                      "pageSize": 20,
                      "first": true,
                      "last": false,
                      "hasNext": true,
                      "hasPrevious": false
                    }"""))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid JWT token"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    @GetMapping("recommendations")
    public ResponseEntity<PagedResponseDto<DvdDto>> getDvdRecommendations(
            HttpServletRequest request,
            @Parameter(description = "Page number (zero-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 20)", example = "20") @RequestParam(defaultValue = "20") int size) {
        return recommendationService.handleGetDvdRecommendations(request, page, size);
    }

    @Operation(summary = "Partially update authenticated user", description = "Updates one or more fields of the authenticated user. User ID is extracted from the JWT token. At least one field must be provided. All fields are optional but at least one is required. Returns the updated avatar path if avatar was changed.", security = {
            @SecurityRequirement(name = "accessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "User successfully updated. Returns avatar path if avatar was updated.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "422", description = "Validation error - invalid field value or no fields provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    @PatchMapping("/edit")
    public ResponseEntity<UserDto> editUser(
            HttpServletRequest httpServletRequest,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Partial user update data. At least one field must be provided.", required = true, content = @Content(schema = @Schema(example = """
                    {
                      "nickname": "NewNickname",
                      "age": 25,
                      "preferredGenresIdentifiers": [1, 5, 12],
                      "password": "NewPassword123"
                    }"""))) @RequestBody UserDto userDto) {
        return userService.handleEditUser(httpServletRequest, userDto);
    }

}
