package pl.kielce.tu.backend.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import pl.kielce.tu.backend.model.dto.ReservationDto;
import pl.kielce.tu.backend.service.reservation.ReservationService;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @Mock
    private HttpServletRequest request;

    private ReservationController reservationController;

    @BeforeEach
    void setUp() {
        reservationController = new ReservationController(reservationService);
    }

    @Test
    void getUserReservations_delegatesToService_andReturnsResponse() {
        List<ReservationDto> dtoList = List.of(new ReservationDto());
        ResponseEntity<List<ReservationDto>> expected = ResponseEntity.ok(dtoList);

        when(reservationService.handleGetUserReservations(request, null)).thenReturn(expected);

        ResponseEntity<List<ReservationDto>> actual = reservationController.getUserReservations(request, null);

        assertSame(expected, actual);
        verify(reservationService).handleGetUserReservations(request, null);
    }

    @Test
    void getUserReservations_withFilter_delegatesToService_andReturnsResponse() {
        String filter = "PENDING";
        List<ReservationDto> dtoList = List.of(new ReservationDto());
        ResponseEntity<List<ReservationDto>> expected = ResponseEntity.ok(dtoList);

        when(reservationService.handleGetUserReservations(request, filter)).thenReturn(expected);

        ResponseEntity<List<ReservationDto>> actual = reservationController.getUserReservations(request, filter);

        assertSame(expected, actual);
        verify(reservationService).handleGetUserReservations(request, filter);
    }

    @Test
    void createReservation_delegatesToService_andReturnsResponse() {
        ReservationDto dto = new ReservationDto();
        ResponseEntity<Void> expected = ResponseEntity.status(HttpStatus.CREATED).build();

        when(reservationService.handleCreateReservation(request, dto)).thenReturn(expected);

        ResponseEntity<Void> actual = reservationController.createReservation(request, dto);

        assertSame(expected, actual);
        verify(reservationService).handleCreateReservation(request, dto);
    }

    @Test
    void acceptReservation_delegatesToService_andReturnsResponse() {
        String id = "reservation-id-1";
        ResponseEntity<Void> expected = ResponseEntity.ok().build();

        when(reservationService.handleAcceptReservation(id)).thenReturn(expected);

        ResponseEntity<Void> actual = reservationController.acceptReservation(id);

        assertSame(expected, actual);
        verify(reservationService).handleAcceptReservation(id);
    }

    @Test
    void declineReservation_delegatesToService_andReturnsResponse() {
        String id = "reservation-id-2";
        ResponseEntity<Void> expected = ResponseEntity.ok().build();

        when(reservationService.handleDeclineReservation(id)).thenReturn(expected);

        ResponseEntity<Void> actual = reservationController.declineReservation(id);

        assertSame(expected, actual);
        verify(reservationService).handleDeclineReservation(id);
    }
}
