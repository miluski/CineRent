package pl.kielce.tu.backend.service.rental.strategy;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.kielce.tu.backend.model.entity.Rental;

@Schema(description = "Strategy for processing rental return requests")
public interface ReturnRequestStrategy {

    @Schema(description = "Process a return request for a rental")
    void processReturnRequest(Rental rental);

    @Schema(description = "Check if the rental can be processed for return request")
    boolean canProcess(Rental rental);
}
