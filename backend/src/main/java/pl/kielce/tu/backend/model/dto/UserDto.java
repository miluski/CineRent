package pl.kielce.tu.backend.model.dto;

import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User data transfer object for managing user information in the CineRent system")
public class UserDto implements Serializable {

    @NotBlank(message = "Nickname is required and cannot be blank")
    @Size(min = 3, max = 50, message = "Nickname must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Nickname can only contain letters, numbers, underscores, and hyphens")
    @Schema(description = "User's unique nickname used for authentication and identification. Must contain only letters, numbers, underscores, and hyphens", example = "john_doe_123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nickname;

    @NotBlank(message = "Password is required and cannot be blank")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Schema(description = "User's password for authentication. Must be between 8-100 characters for security", example = "SecurePassword123!", requiredMode = Schema.RequiredMode.REQUIRED, format = "password")
    private String password;

    @NotNull(message = "Age is required")
    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 149, message = "Age cannot exceed 149")
    @Schema(description = "User's age in years. Required for age-appropriate content recommendations and compliance", example = "25", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1", maximum = "149")
    private Integer age;

    @Schema(description = "List of preferred genre identifiers. Each ID must exist in the genres database table. Used for personalized movie recommendations", example = "[1, 5, 12, 18]")
    private List<Long> preferredGenresIdentifiers;

    @Schema(description = "List of preferred genre names (read-only). Automatically populated based on preferredGenresIdentifiers", example = "[\"Action\", \"Comedy\", \"Sci-Fi\", \"Drama\"]", accessMode = Schema.AccessMode.READ_ONLY)
    private List<String> preferredGenres;

}
