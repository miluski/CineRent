package pl.kielce.tu.backend.service.validation.implementations.dvd;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class DvdPriceValidationStrategy implements FieldValidationStrategy<Float> {

    private final UserContextLogger userContextLogger;

    @Override
    public void validate(Float rentalPricePerDay) throws ValidationException {
        String priceInfo = rentalPricePerDay != null ? rentalPricePerDay + " per day" : "null";
        userContextLogger.logValidationOperation("DVD_PRICE", "STARTED", priceInfo);

        validateNotNull(rentalPricePerDay);
        validateRange(rentalPricePerDay);

        userContextLogger.logValidationOperation("DVD_PRICE", "SUCCESS", priceInfo);
    }

    @Override
    public ValidationStrategyType getStrategyType() {
        return ValidationStrategyType.DVD_PRICE;
    }

    private void validateNotNull(Float rentalPricePerDay) throws ValidationException {
        if (rentalPricePerDay == null) {
            throw new ValidationException("Rental price is required");
        }
    }

    private void validateRange(Float rentalPricePerDay) throws ValidationException {
        if (rentalPricePerDay <= 0.0f) {
            throw new ValidationException("Rental price must be greater than 0");
        }
        if (rentalPricePerDay >= 50.0f) {
            throw new ValidationException("Rental price must be less than 50");
        }
    }

}
