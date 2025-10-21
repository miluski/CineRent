package pl.kielce.tu.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a DVD availability reminder")
public class DvdReminderRequestDto {

    @NotNull(message = "DVD ID is required")
    @Positive(message = "DVD ID must be positive")
    @Schema(description = "The ID of the DVD to be notified about", example = "42", required = true)
    private Long dvdId;

}
