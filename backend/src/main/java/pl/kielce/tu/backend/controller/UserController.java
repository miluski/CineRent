package pl.kielce.tu.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.dto.UserDto;
import pl.kielce.tu.backend.service.user.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "User", description = "User management operations")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get authenticated user data", description = "Retrieves the authenticated user's data including nickname, age, and preferred genre names. User ID is extracted from the JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User data retrieved successfully", content = @Content(schema = @Schema(example = """
                    {
                      "nickname": "FilmLover99",
                      "age": 24,
                      "preferredGenres": ["Science-Fiction", "Akcja", "Dramat"]
                    }"""))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping
    public ResponseEntity<UserDto> getUser(HttpServletRequest httpServletRequest) {
        return userService.handleGetUser(httpServletRequest);
    }

    @Operation(summary = "Partially update authenticated user", description = "Updates one or more fields of the authenticated user. User ID is extracted from the JWT token. At least one field must be provided. All fields are optional but at least one is required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "User successfully updated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "422", description = "Validation error - invalid field value or no fields provided")
    })
    @PatchMapping("/edit")
    public ResponseEntity<Void> editUser(
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
