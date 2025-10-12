package pl.kielce.tu.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Genre data transfer object for managing Genre information in the rental system")
public class GenreDto {

    @Schema(description = "Unique identifier of the Genre", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 5, max = 75, message = "Name must be between 5 and 75 characters")
    @Schema(description = "Name of the Genre", example = "Science-Fiction", required = true)
    private String name;

}
