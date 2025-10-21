package pl.kielce.tu.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.dto.UserDto;
import pl.kielce.tu.backend.service.auth.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = """
            Creates a new user account with the provided credentials and preferences. \
            Required fields: nickname (3-50 characters, alphanumeric with underscores/hyphens), \
            email (valid email address), password (8-100 characters), and age (1-149 years). \
            Optional: preferred genre identifiers (must exist in the database). \
            A verification email with a 6-digit code will be sent to the provided email address.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered, verification email sent"),
            @ApiResponse(responseCode = "422", description = "Validation failed - invalid user data (invalid nickname/password/email format, age out of range 1-149, non-existent genre identifiers, or duplicate nickname/email)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred during registration", content = @Content)
    })
    public ResponseEntity<Void> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User registration data", required = true, content = @Content(schema = @Schema(example = """
                    {
                      "nickname": "FilmLover99",
                      "email": "filmlover99@example.com",
                      "password": "Serduszko223",
                      "age": 24,
                      "preferredGenresIdentifiers": [1, 10, 21, 37]
                    }"""))) @RequestBody(required = true) UserDto userDto) {
        return authService.handleRegister(userDto);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = """
            Authenticates a user with the provided credentials and returns JWT tokens as HTTP-only cookies. \
            Required fields: nickname and password. Age and preferred genres are not required for login. \
            Email verification is not required for login - users can log in immediately after registration. \
            On successful login, access and refresh tokens are set as secure HTTP-only cookies.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully authenticated, tokens set in cookies"),
            @ApiResponse(responseCode = "401", description = "Authentication failed - invalid credentials or user not found", content = @Content),
            @ApiResponse(responseCode = "422", description = "Validation failed - invalid user data format", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred during login", content = @Content)
    })
    public ResponseEntity<Void> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User login credentials", required = true, content = @Content(schema = @Schema(example = """
                    {
                      "nickname": "FilmLover99",
                      "password": "Serduszko223"
                    }"""))) @RequestBody(required = true) UserDto userDto,
            HttpServletResponse httpServletResponse) {
        return authService.handleLogin(userDto, httpServletResponse);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = """
            Logs out the authenticated user by blacklisting their current access and refresh tokens \
            and removing the token cookies. This ensures the tokens cannot be used again. \
            The user must obtain new tokens through login to authenticate again.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully logged out, tokens blacklisted and cookies cleared"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred during logout", content = @Content)
    })
    public ResponseEntity<Void> logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return authService.handleLogout(httpServletRequest, httpServletResponse);
    }

    @PostMapping("/refresh-tokens")
    @Operation(summary = "Refresh authentication tokens", description = """
            Refreshes the user's access and refresh tokens using the current valid refresh token. \
            The old tokens are blacklisted and new tokens are generated. \
            This allows maintaining the user session without requiring re-authentication. \
            The refresh token must be present in the request cookies and not blacklisted.""", security = {
            @SecurityRequirement(name = "refreshToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens successfully refreshed, new tokens set in cookies"),
            @ApiResponse(responseCode = "401", description = "Refresh failed - invalid or expired refresh token, or user not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred during token refresh", content = @Content)
    })
    public ResponseEntity<Void> refreshTokens(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        return authService.handleRefreshTokens(httpServletRequest, httpServletResponse);
    }

}
