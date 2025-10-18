package pl.kielce.tu.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of expired rental processing operation")
public class ExpiredRentalProcessingDto {

    @Schema(description = "Total number of expired rentals found", example = "10")
    private Integer totalExpiredRentals;

    @Schema(description = "Number of rentals successfully processed", example = "9")
    private Integer processedSuccessfully;

    @Schema(description = "Number of rentals that failed to process", example = "1")
    private Integer failedToProcess;
}
