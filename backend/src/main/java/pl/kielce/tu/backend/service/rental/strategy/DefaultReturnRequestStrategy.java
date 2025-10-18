package pl.kielce.tu.backend.service.rental.strategy;

import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.constant.RentalStatus;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.repository.RentalRepository;

@Component
@RequiredArgsConstructor
@Schema(description = "Default implementation for processing return requests")
public class DefaultReturnRequestStrategy implements ReturnRequestStrategy {

    private final RentalRepository rentalRepository;

    @Override
    public void processReturnRequest(Rental rental) {
        rental.setStatus(RentalStatus.RETURN_REQUESTED);
        rentalRepository.save(rental);
    }

    @Override
    public boolean canProcess(Rental rental) {
        return rental != null
                && rental.getStatus() == RentalStatus.ACTIVE;
    }
}
