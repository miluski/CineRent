package pl.kielce.tu.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.kielce.tu.backend.model.entity.Reservation;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for validating reservation cancellation operations")
public class ReservationCancellationRequest {

    @Schema(description = "Reservation entity to be cancelled", required = true)
    private Reservation reservation;

    @Schema(description = "ID of user attempting to cancel reservation", required = true)
    private Long userId;

}
