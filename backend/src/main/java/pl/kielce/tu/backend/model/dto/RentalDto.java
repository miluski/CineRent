package pl.kielce.tu.backend.model.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.kielce.tu.backend.model.constant.RentalStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Rental data transfer object for managing rental information in the rental system")
public class RentalDto {

    @Schema(description = "Unique identifier of the rental", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "Rental start date and time is required")
    @FutureOrPresent(message = "Rental start date and time must be in the present or future")
    @Schema(description = "Start date and time of the rental period", example = "2024-01-15T10:00:00", required = true)
    private LocalDateTime rentalStart;

    @NotNull(message = "Rental end date and time is required")
    @Future(message = "Rental end date and time must be in the future")
    @Schema(description = "End date and time of the rental period", example = "2024-01-22T10:00:00", required = true)
    private LocalDateTime rentalEnd;

    @Schema(description = "Actual return date and time of the rental", example = "2024-01-20T14:30:00")
    private LocalDateTime returnDate;

    @Schema(description = "Creation timestamp of the rental", example = "2024-01-10T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @NotNull(message = "DVD ID is required")
    @Positive(message = "DVD ID must be positive")
    @Schema(description = "ID of the rented DVD", example = "42", required = true)
    private Long dvdId;

    @Schema(description = "Title of the rented DVD", example = "The Matrix", accessMode = Schema.AccessMode.READ_ONLY)
    private String dvdTitle;

    @NotNull(message = "Count is required")
    @Positive(message = "Count must be positive")
    @Schema(description = "Number of DVD copies rented", example = "2", required = true)
    private Integer count;

    @Schema(description = "Current status of the rental", example = "ACTIVE", accessMode = Schema.AccessMode.READ_ONLY)
    private RentalStatus status;

}
