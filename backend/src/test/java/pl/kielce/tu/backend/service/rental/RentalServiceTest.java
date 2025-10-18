package pl.kielce.tu.backend.service.rental;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import pl.kielce.tu.backend.extractor.ClaimsExtractor;
import pl.kielce.tu.backend.mapper.RentalFilterMapper;
import pl.kielce.tu.backend.mapper.RentalMapper;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.constant.RentalStatus;
import pl.kielce.tu.backend.model.dto.RentalDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.repository.RentalRepository;
import pl.kielce.tu.backend.service.auth.CookieService;
import pl.kielce.tu.backend.service.dvd.DvdAvailabilityService;
import pl.kielce.tu.backend.service.rental.strategy.ReturnRequestStrategy;
import pl.kielce.tu.backend.service.rental.transaction.TransactionGeneratorService;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private RentalMapper rentalMapper;
    @Mock
    private CookieService cookieService;
    @Mock
    private ClaimsExtractor claimsExtractor;
    @Mock
    private RentalFilterMapper filterMapper;
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private DvdAvailabilityService dvdAvailabilityService;
    @Mock
    private TransactionGeneratorService transactionGenerator;
    @Mock
    private ReturnRequestStrategy returnRequestStrategy;
    @InjectMocks
    private RentalService rentalService;

    @Mock
    private HttpServletRequest request;

    @SuppressWarnings("null")
    @Test
    void handleGetUserRentals_returnsOkWithDtos() {
        String token = "token";
        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(token);
        when(claimsExtractor.extractUserId(token, null)).thenReturn(1L);
        when(filterMapper.mapFilterToStatus("filter")).thenReturn(RentalStatus.ACTIVE);

        Rental rental = new Rental();
        rental.setStatus(RentalStatus.ACTIVE);
        List<Rental> rentals = Collections.singletonList(rental);
        when(rentalRepository.findByUserIdWithOptionalStatus(eq(1L), eq(RentalStatus.ACTIVE), any()))
                .thenReturn(new PageImpl<>(rentals));

        RentalDto dto = new RentalDto();
        when(rentalMapper.toDtoList(rentals)).thenReturn(Collections.singletonList(dto));

        ResponseEntity<List<RentalDto>> response = rentalService.handleGetUserRentals(request, "filter");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(rentalRepository).findByUserIdWithOptionalStatus(eq(1L), eq(RentalStatus.ACTIVE), any());
        verify(rentalMapper).toDtoList(rentals);
    }

    @Test
    void handleGetUserRentals_whenNoToken_returnsInternalServerError() {
        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(null);

        ResponseEntity<List<RentalDto>> response = rentalService.handleGetUserRentals(request, "any");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void handleReturnDvd_whenActive_setsReturnRequestedAndReturnsAccepted() {
        when(filterMapper.parseRentalId("1")).thenReturn(1L);
        Rental rental = new Rental();
        rental.setStatus(RentalStatus.ACTIVE);
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

        ResponseEntity<Void> response = rentalService.handleReturnDvd("1");

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(returnRequestStrategy).processReturnRequest(rental);
    }

    @Test
    void handleReturnDvd_whenNotActive_returnsBadRequest() {
        when(filterMapper.parseRentalId("2")).thenReturn(2L);
        Rental rental = new Rental();
        rental.setStatus(RentalStatus.INACTIVE);
        when(rentalRepository.findById(2L)).thenReturn(Optional.of(rental));

        ResponseEntity<Void> response = rentalService.handleReturnDvd("2");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void handleReturnDvd_whenNotFound_returnsNotFound() {
        when(filterMapper.parseRentalId("3")).thenReturn(3L);
        when(rentalRepository.findById(3L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = rentalService.handleReturnDvd("3");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleAcceptReturn_whenRequested_completesRentalAndReturnsAccepted() {
        when(filterMapper.parseRentalId("4")).thenReturn(4L);
        Rental rental = new Rental();
        rental.setStatus(RentalStatus.RETURN_REQUESTED);
        rental.setCount(3);
        Dvd dvd = new Dvd();
        rental.setDvd(dvd);
        when(rentalRepository.findById(4L)).thenReturn(Optional.of(rental));
        when(transactionGenerator.generateTransaction(rental)).thenReturn(null);

        ResponseEntity<Void> response = rentalService.handleAcceptReturn("4");

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals(RentalStatus.INACTIVE, rental.getStatus());
        assertNotNull(rental.getReturnDate());
        verify(transactionGenerator).generateTransaction(rental);
        verify(dvdAvailabilityService).increaseAvailability(dvd, 3);
        verify(rentalRepository).save(rental);
    }

    @Test
    void handleAcceptReturn_whenWrongStatus_returnsBadRequest() {
        when(filterMapper.parseRentalId("5")).thenReturn(5L);
        Rental rental = new Rental();
        rental.setStatus(RentalStatus.ACTIVE);
        when(rentalRepository.findById(5L)).thenReturn(Optional.of(rental));

        ResponseEntity<Void> response = rentalService.handleAcceptReturn("5");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void handleDeclineReturn_whenRequested_setsActiveAndReturnsOk() {
        when(filterMapper.parseRentalId("6")).thenReturn(6L);
        Rental rental = new Rental();
        rental.setStatus(RentalStatus.RETURN_REQUESTED);
        when(rentalRepository.findById(6L)).thenReturn(Optional.of(rental));

        ResponseEntity<Void> response = rentalService.handleDeclineReturn("6");

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals(RentalStatus.ACTIVE, rental.getStatus());
        verify(rentalRepository).save(rental);
    }

    @Test
    void handleDeclineReturn_whenWrongStatus_returnsBadRequest() {
        when(filterMapper.parseRentalId("7")).thenReturn(7L);
        Rental rental = new Rental();
        rental.setStatus(RentalStatus.INACTIVE);
        when(rentalRepository.findById(7L)).thenReturn(Optional.of(rental));

        ResponseEntity<Void> response = rentalService.handleDeclineReturn("7");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(rentalRepository, never()).save(any());
    }
}
