package pl.kielce.tu.backend.model.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DVD data transfer object for managing DVD information in the rental system")
public class DvdDto implements Serializable {

    @Schema(description = "Unique identifier of the DVD", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 75, message = "Title must be between 5 and 75 characters")
    @Schema(description = "Title of the DVD", example = "The Matrix", required = true)
    private String title;

    @Schema(description = "List of genre names associated with the DVD", example = "[\"Action\", \"Sci-Fi\"]", accessMode = Schema.AccessMode.READ_ONLY)
    private List<String> genres;

    @NotEmpty(message = "At least one genre must be selected")
    @Schema(description = "List of genre IDs that must exist in the genres table", example = "[1, 2]", required = true)
    private List<Long> genresIdentifiers;

    @NotNull(message = "Release year is required")
    @Min(value = 1000, message = "Release year must be greater than 1000")
    @Max(value = 2025, message = "Release year cannot be in the future")
    @Schema(description = "Year when the DVD was released", example = "1999", required = true)
    private Integer releaseYear;

    @NotEmpty(message = "At least one director is required")
    @Schema(description = "List of directors of the DVD", required = true)
    private List<@NotBlank(message = "Director name cannot be blank") @Size(min = 10, max = 50, message = "Director name must be between 10 and 50 characters") String> directors;

    @NotBlank(message = "Description is required")
    @Size(min = 25, max = 500, message = "Description must be between 25 and 500 characters")
    @Schema(description = "Detailed description of the DVD content", example = "A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers.", required = true)
    private String description;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be greater than 0 minutes")
    @Schema(description = "Duration of the DVD in minutes", example = "136", required = true)
    private Integer durationMinutes;

    @Schema(description = "Availability status for simplified list view", example = "AVALAIBLE", accessMode = Schema.AccessMode.READ_ONLY)
    private String status;

    @NotNull(message = "Availability status is required")
    @Schema(description = "Whether the DVD is available for rental", example = "true", required = true)
    private Boolean available;

    @NotNull(message = "Number of available copies is required")
    @Min(value = 0, message = "Available copies cannot be negative")
    @Max(value = 100, message = "Available copies cannot exceed 100")
    @Schema(description = "Number of copies available for rental", example = "5", required = true)
    private Integer copiesAvailable;

    @NotNull(message = "Rental price is required")
    @DecimalMin(value = "0.01", message = "Rental price must be greater than 0")
    @DecimalMax(value = "49.99", message = "Rental price must be less than 50")
    @Schema(description = "Daily rental price in currency units", example = "4.99", required = true)
    private Float rentalPricePerDay;

    @Schema(description = "URL to the DVD poster image", example = "https://example.com/posters/matrix.jpg")
    private String posterUrl;

    @Schema(description = "Base64 encoded poster image data", example = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD...")
    private String posterImage;

    @Schema(description = "Timestamp when the DVD was added to the system", example = "2025-10-11T14:30:15", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime addedAt;

}
