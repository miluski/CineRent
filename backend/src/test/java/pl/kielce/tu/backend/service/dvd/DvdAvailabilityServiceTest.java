package pl.kielce.tu.backend.service.dvd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.repository.DvdRepository;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class DvdAvailabilityServiceTest {

    @Mock
    private DvdRepository dvdRepository;

    @Mock
    private UserContextLogger userContextLogger;

    private DvdAvailabilityService dvdAvailabilityService;

    @BeforeEach
    void setUp() {
        dvdAvailabilityService = new DvdAvailabilityService(dvdRepository, userContextLogger);
    }

    @Test
    void shouldDecreaseAvailability() {
        Dvd dvd = new Dvd();
        dvd.setCopiesAvalaible(3);
        dvd.setAvalaible(true);

        dvdAvailabilityService.decreaseAvailability(dvd, 1);

        assertEquals(2, dvd.getCopiesAvalaible());
        assertTrue(dvd.getAvalaible());
        verify(dvdRepository).save(dvd);
    }

    @Test
    void shouldSetUnavailableWhenLastCopyReserved() {
        Dvd dvd = new Dvd();
        dvd.setCopiesAvalaible(1);
        dvd.setAvalaible(true);

        dvdAvailabilityService.decreaseAvailability(dvd, 1);

        assertEquals(0, dvd.getCopiesAvalaible());
        assertFalse(dvd.getAvalaible());
        verify(dvdRepository).save(dvd);
    }

    @Test
    void shouldThrowExceptionWhenNoCopiesAvailable() {
        Dvd dvd = new Dvd();
        dvd.setCopiesAvalaible(0);
        dvd.setAvalaible(false);

        assertThrows(IllegalStateException.class, () -> dvdAvailabilityService.decreaseAvailability(dvd, 1));
    }

    @Test
    void shouldIncreaseAvailability() {
        Dvd dvd = new Dvd();
        dvd.setCopiesAvalaible(0);
        dvd.setAvalaible(false);

        dvdAvailabilityService.increaseAvailability(dvd, 1);

        assertEquals(1, dvd.getCopiesAvalaible());
        assertTrue(dvd.getAvalaible());
        verify(dvdRepository).save(dvd);
    }
}
