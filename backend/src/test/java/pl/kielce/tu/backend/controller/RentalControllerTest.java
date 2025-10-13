package pl.kielce.tu.backend.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import pl.kielce.tu.backend.model.constant.FilterConstants;
import pl.kielce.tu.backend.model.dto.RentalDto;
import pl.kielce.tu.backend.service.rental.RentalService;

@ExtendWith(MockitoExtension.class)
class RentalControllerTest {

    @Mock
    private RentalService rentalService;

    @Mock
    private HttpServletRequest request;

    private RentalController rentalController;

    @BeforeEach
    void setUp() {
        rentalController = new RentalController(rentalService);
    }

    @Test
    void shouldGetUserRentals_forwardToService_andReturnResponse() {
        List<RentalDto> emptyList = Collections.emptyList();
        ResponseEntity<List<RentalDto>> expected = ResponseEntity.ok(emptyList);

        when(rentalService.handleGetUserRentals(request, FilterConstants.HISTORICAL.getValue())).thenReturn(expected);

        ResponseEntity<List<RentalDto>> actual = rentalController.getUserRentals(request,
                FilterConstants.HISTORICAL.getValue());

        assertSame(expected, actual);
        verify(rentalService).handleGetUserRentals(request, FilterConstants.HISTORICAL.getValue());
    }

    @Test
    void shouldReturnDvd_forwardToService_andReturnResponse() {
        String id = "rental-1";
        ResponseEntity<Void> expected = ResponseEntity.ok().build();

        when(rentalService.handleReturnDvd(id)).thenReturn(expected);

        ResponseEntity<Void> actual = rentalController.returnDvd(id);

        assertSame(expected, actual);
        verify(rentalService).handleReturnDvd(id);
    }

    @Test
    void shouldAcceptReturn_forwardToService_andReturnResponse() {
        String id = "rental-2";
        ResponseEntity<Void> expected = ResponseEntity.ok().build();

        when(rentalService.handleAcceptReturn(id)).thenReturn(expected);

        ResponseEntity<Void> actual = rentalController.acceptReturn(id);

        assertSame(expected, actual);
        verify(rentalService).handleAcceptReturn(id);
    }

    @Test
    void shouldDeclineReturn_forwardToService_andReturnResponse() {
        String id = "rental-3";
        ResponseEntity<Void> expected = ResponseEntity.ok().build();

        when(rentalService.handleDeclineReturn(id)).thenReturn(expected);

        ResponseEntity<Void> actual = rentalController.declineReturn(id);

        assertSame(expected, actual);
        verify(rentalService).handleDeclineReturn(id);
    }
}
