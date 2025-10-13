package pl.kielce.tu.backend.service.reservation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.List;
import java.util.Objects;
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
import pl.kielce.tu.backend.mapper.ReservationFilterMapper;
import pl.kielce.tu.backend.mapper.ReservationMapper;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.constant.ReservationStatus;
import pl.kielce.tu.backend.model.dto.ReservationDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.model.entity.Reservation;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.DvdRepository;
import pl.kielce.tu.backend.repository.RentalRepository;
import pl.kielce.tu.backend.repository.ReservationRepository;
import pl.kielce.tu.backend.repository.UserRepository;
import pl.kielce.tu.backend.service.auth.CookieService;
import pl.kielce.tu.backend.service.dvd.DvdAvailabilityService;
import pl.kielce.tu.backend.service.rental.factory.RentalFactory;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private DvdRepository dvdRepository;
    @Mock
    private RentalFactory rentalFactory;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CookieService cookieService;
    @Mock
    private ClaimsExtractor claimsExtractor;
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private ReservationFilterMapper filterMapper;
    @Mock
    private DvdAvailabilityService availabilityService;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationValidationService reservationValidationService;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void handleGetUserReservations_returnsOkWithDtoList() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "token";
        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(token);
        when(claimsExtractor.extractUserId(eq(token), any())).thenReturn(42L);
        when(filterMapper.mapFilterToStatus("all")).thenReturn(null);

        Reservation reservation = mock(Reservation.class);
        List<Reservation> reservations = List.of(reservation);
        when(reservationRepository.findByUserIdWithOptionalStatus(eq(42L), eq(null), any()))
                .thenReturn(new PageImpl<>(reservations));

        ReservationDto dto = new ReservationDto();
        when(reservationMapper.toDtoList(reservations)).thenReturn(List.of(dto));

        ResponseEntity<List<ReservationDto>> response = reservationService.handleGetUserReservations(request, "all");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, Objects.requireNonNull(response.getBody()).size());
        verify(reservationMapper).toDtoList(reservations);
    }

    @Test
    void handleCreateReservation_returnsCreatedAndSavesReservation() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "token";
        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(token);
        when(claimsExtractor.extractUserId(eq(token), any())).thenReturn(7L);

        ReservationDto dto = new ReservationDto();
        dto.setDvdId(11L);
        dto.setRentalStart(new Date(System.currentTimeMillis()));
        dto.setRentalEnd(new Date(System.currentTimeMillis() + 1000L));
        dto.setCount(1);

        User user = mock(User.class);
        Dvd dvd = mock(Dvd.class);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(dvdRepository.findById(11L)).thenReturn(Optional.of(dvd));

        ResponseEntity<Void> response = reservationService.handleCreateReservation(request, dto);

        assert response.getStatusCode() == HttpStatus.CREATED;
        verify(availabilityService).decreaseAvailability(dvd, 1);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void handleCreateReservation_returnsBadRequestWhenInvalidDto() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "token";
        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(token);
        when(claimsExtractor.extractUserId(eq(token), any())).thenReturn(7L);

        ReservationDto dto = new ReservationDto();

        ResponseEntity<Void> response = reservationService.handleCreateReservation(request, dto);

        assert response.getStatusCode() == HttpStatus.BAD_REQUEST;
        verifyNoInteractions(dvdRepository, userRepository, reservationRepository, availabilityService);
    }

    @Test
    void handleAcceptReservation_success_createsRentalAndAccepts() {
        String id = "5";
        when(filterMapper.parseReservationId(id)).thenReturn(5L);

        Reservation reservation = mock(Reservation.class);
        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));
        when(reservation.getStatus()).thenReturn(ReservationStatus.PENDING);

        Rental rental = new Rental();
        when(rentalFactory.createFromReservation(reservation)).thenReturn(rental);

        ResponseEntity<Void> response = reservationService.handleAcceptReservation(id);

        assert response.getStatusCode() == HttpStatus.ACCEPTED;
        verify(rentalFactory).createFromReservation(reservation);
        verify(rentalRepository).save(rental);
        verify(reservationRepository).save(reservation);
    }

    @Test
    void handleAcceptReservation_badRequestWhenNotPending() {
        String id = "6";
        when(filterMapper.parseReservationId(id)).thenReturn(6L);

        Reservation reservation = mock(Reservation.class);
        when(reservationRepository.findById(6L)).thenReturn(Optional.of(reservation));
        when(reservation.getStatus()).thenReturn(ReservationStatus.ACCEPTED);

        ResponseEntity<Void> response = reservationService.handleAcceptReservation(id);

        assert response.getStatusCode() == HttpStatus.BAD_REQUEST;
        verify(rentalFactory, never()).createFromReservation(any());
    }

    @Test
    void handleAcceptReservation_notFoundWhenMissing() {
        String id = "999";
        when(filterMapper.parseReservationId(id)).thenReturn(999L);
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = reservationService.handleAcceptReservation(id);

        assert response.getStatusCode() == HttpStatus.NOT_FOUND;
    }

    @Test
    void handleDeclineReservation_success_increasesAvailabilityAndRejects() {
        String id = "8";
        when(filterMapper.parseReservationId(id)).thenReturn(8L);

        Reservation reservation = mock(Reservation.class);
        Dvd dvd = mock(Dvd.class);
        when(reservationRepository.findById(8L)).thenReturn(Optional.of(reservation));
        when(reservation.getStatus()).thenReturn(ReservationStatus.PENDING);
        when(reservation.getDvd()).thenReturn(dvd);
        when(reservation.getCount()).thenReturn(2);

        ResponseEntity<Void> response = reservationService.handleDeclineReservation(id);

        assert response.getStatusCode() == HttpStatus.ACCEPTED;
        verify(availabilityService).increaseAvailability(dvd, 2);
        verify(reservationRepository).save(reservation);
    }

    @Test
    void handleCancelReservation_success_increasesAvailabilityAndCancels() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String id = "10";
        String token = "valid-token";

        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(token);
        when(claimsExtractor.extractUserId(eq(token), any())).thenReturn(42L);
        when(filterMapper.parseReservationId(id)).thenReturn(10L);

        Reservation reservation = mock(Reservation.class);
        Dvd dvd = mock(Dvd.class);
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        when(reservation.getDvd()).thenReturn(dvd);
        when(reservation.getCount()).thenReturn(3);

        ResponseEntity<Void> response = reservationService.handleCancelReservation(request, id);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(reservationValidationService).validateReservationCancellation(reservation, 42L);
        verify(availabilityService).increaseAvailability(dvd, 3);
        verify(reservation).setStatus(ReservationStatus.CANCELLED);
        verify(reservationRepository).save(reservation);
    }

    @Test
    void handleCancelReservation_validationError_returnsBadRequest() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String id = "11";
        String token = "valid-token";

        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(token);
        when(claimsExtractor.extractUserId(eq(token), any())).thenReturn(42L);
        when(filterMapper.parseReservationId(id)).thenReturn(11L);

        Reservation reservation = mock(Reservation.class);
        when(reservationRepository.findById(11L)).thenReturn(Optional.of(reservation));

        doThrow(new pl.kielce.tu.backend.exception.ValidationException("Validation error"))
                .when(reservationValidationService).validateReservationCancellation(any(), any());

        ResponseEntity<Void> response = reservationService.handleCancelReservation(request, id);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(availabilityService, never()).increaseAvailability(any(), any());
        verify(reservationRepository, never()).save(reservation);
    }

    @Test
    void handleCancelReservation_reservationNotFound_returnsNotFound() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String id = "999";
        String token = "valid-token";

        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(token);
        when(claimsExtractor.extractUserId(eq(token), any())).thenReturn(42L);
        when(filterMapper.parseReservationId(id)).thenReturn(999L);
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = reservationService.handleCancelReservation(request, id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verifyNoInteractions(reservationValidationService);
        verifyNoInteractions(availabilityService);
    }
}
