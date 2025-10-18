package pl.kielce.tu.backend.service.rental.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.model.constant.RentalStatus;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.repository.RentalRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultReturnRequestStrategy Tests")
class DefaultReturnRequestStrategyTest {

    @Mock
    private RentalRepository rentalRepository;

    @InjectMocks
    private DefaultReturnRequestStrategy strategy;

    private Rental rental;

    @BeforeEach
    void setUp() {
        rental = Rental.builder()
                .id(1L)
                .status(RentalStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should process return request for active rental")
    void shouldProcessReturnRequestForActiveRental() {

        strategy.processReturnRequest(rental);

        assertEquals(RentalStatus.RETURN_REQUESTED, rental.getStatus());
        verify(rentalRepository, times(1)).save(rental);
    }

    @Test
    @DisplayName("Should return true when can process active rental")
    void shouldReturnTrueWhenCanProcessActiveRental() {

        boolean result = strategy.canProcess(rental);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when rental is null")
    void shouldReturnFalseWhenRentalIsNull() {

        boolean result = strategy.canProcess(null);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when rental is not active")
    void shouldReturnFalseWhenRentalIsNotActive() {

        rental.setStatus(RentalStatus.INACTIVE);

        boolean result = strategy.canProcess(rental);

        assertFalse(result);
    }
}
