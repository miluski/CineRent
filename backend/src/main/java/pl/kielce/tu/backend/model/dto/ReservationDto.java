package pl.kielce.tu.backend.model.dto;

import java.sql.Date;
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
import pl.kielce.tu.backend.model.constant.ReservationStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reservation data transfer object for managing reservation information in the rental system")
public class ReservationDto {

    @Schema(description = "Unique identifier of the reservation", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "Reservation start date is required")
    @FutureOrPresent(message = "Reservation start date must be in the present or future")
    @Schema(description = "Start date of the reservation period", example = "2024-01-15", required = true)
    private Date rentalStart;

    @NotNull(message = "Reservation end date is required")
    @Future(message = "Reservation end date must be in the future")
    @Schema(description = "End date of the reservation period", example = "2024-01-22", required = true)
    private Date rentalEnd;

    @Schema(description = "Creation timestamp of the reservation", example = "2024-01-10T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @NotNull(message = "DVD ID is required")
    @Positive(message = "DVD ID must be positive")
    @Schema(description = "ID of the reserved DVD", example = "42", required = true)
    private Long dvdId;

    @Schema(description = "Title of the reserved DVD", example = "The Matrix", accessMode = Schema.AccessMode.READ_ONLY)
    private String dvdTitle;

    @NotNull(message = "Count is required")
    @Positive(message = "Count must be positive")
    @Schema(description = "Number of DVD copies reserved", example = "2", required = true)
    private Integer count;

    @Schema(description = "Current status of the reservation", example = "PENDING", accessMode = Schema.AccessMode.READ_ONLY)
    private ReservationStatus status;

}
