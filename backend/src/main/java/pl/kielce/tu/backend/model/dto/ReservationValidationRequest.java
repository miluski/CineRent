package pl.kielce.tu.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.kielce.tu.backend.model.entity.Dvd;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for validating reservation operations including copy count and availability checks")
public class ReservationValidationRequest {

    @Schema(description = "DVD entity being reserved with availability and copy information", required = true)
    private Dvd dvd;

    @Schema(description = "Reservation data containing rental dates, count, and user information", required = true)
    private ReservationDto reservation;

}
