package pl.kielce.tu.backend.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import pl.kielce.tu.backend.model.dto.DvdDto;
import pl.kielce.tu.backend.model.dto.PagedResponseDto;
import pl.kielce.tu.backend.service.dvd.DvdService;

@ExtendWith(MockitoExtension.class)
class DvdControllerTest {

    @Mock
    private DvdService dvdService;

    @InjectMocks
    private DvdController dvdController;

    @Test
    void getAllDvds_delegatesToService_andReturnsResponse() {
        PagedResponseDto<DvdDto> pagedResponse = PagedResponseDto.<DvdDto>builder().build();
        ResponseEntity<PagedResponseDto<DvdDto>> expected = ResponseEntity.ok(pagedResponse);
        when(dvdService.handleGetAllDvdsWithOptionalFilters(null, null, null, 0, 20)).thenReturn(expected);
        ResponseEntity<PagedResponseDto<DvdDto>> actual = dvdController.getAllDvds(null, null, null, 0, 20);
        assertSame(expected, actual);
        verify(dvdService).handleGetAllDvdsWithOptionalFilters(null, null, null, 0, 20);
    }

    @Test
    void getEnhancedDvd_delegatesToService_andReturnsResponse() {
        DvdDto dto = org.mockito.Mockito.mock(DvdDto.class);
        ResponseEntity<DvdDto> expected = ResponseEntity.ok(dto);
        when(dvdService.handleGetDvdById("123")).thenReturn(expected);
        ResponseEntity<DvdDto> actual = dvdController.getEnhancedDvd("123");
        assertSame(expected, actual);
        verify(dvdService).handleGetDvdById("123");
    }

    @Test
    void createDvd_delegatesToService_andReturnsResponse() {
        DvdDto dto = Mockito.mock(DvdDto.class);
        ResponseEntity<Void> expected = ResponseEntity.status(HttpStatus.CREATED).build();
        when(dvdService.handleCreateDvd(dto)).thenReturn(expected);

        ResponseEntity<Void> actual = dvdController.createDvd(dto);

        assertSame(expected, actual);
        verify(dvdService).handleCreateDvd(dto);
    }

    @Test
    void editDvd_delegatesToService_andReturnsResponse() {
        DvdDto dto = Mockito.mock(DvdDto.class);
        ResponseEntity<Void> expected = ResponseEntity.status(HttpStatus.ACCEPTED).build();
        when(dvdService.handleUpdateDvd("42", dto)).thenReturn(expected);
        ResponseEntity<Void> actual = dvdController.editDvd("42", dto);
        assertSame(expected, actual);
        verify(dvdService).handleUpdateDvd("42", dto);
    }
}
