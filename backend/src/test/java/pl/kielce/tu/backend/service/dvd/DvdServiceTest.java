package pl.kielce.tu.backend.service.dvd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.mapper.DvdFilterMapper;
import pl.kielce.tu.backend.mapper.DvdMapper;
import pl.kielce.tu.backend.model.dto.DvdDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.repository.DvdRepository;
import pl.kielce.tu.backend.service.dvd.filter.DvdFilterService;
import pl.kielce.tu.backend.service.resource.ResourceService;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class DvdServiceTest {

    @Mock
    private DvdMapper dvdMapper;
    @Mock
    private DvdRepository dvdRepository;
    @Mock
    private DvdUpdateService updateService;
    @Mock
    private ResourceService resourceService;
    @Mock
    private UserContextLogger userContextLogger;
    @Mock
    private DvdValidationService validationService;
    @Mock
    private DvdFilterMapper dvdFilterMapper;
    @Mock
    private DvdFilterService dvdFilterService;

    private DvdService dvdService;

    @BeforeEach
    void setUp() {
        dvdService = new DvdService(dvdMapper, dvdRepository, updateService, dvdFilterMapper, resourceService,
                dvdFilterService, userContextLogger, validationService);
    }

    @Test
    void handleGetAllDvds_returnsOkWithDtos() {
        Dvd dvd = mock(Dvd.class);
        DvdDto dto = mock(DvdDto.class);
        when(dvdRepository.findAll()).thenReturn(Arrays.asList(dvd));
        when(dvdMapper.toDto(dvd)).thenReturn(dto);

        ResponseEntity<List<DvdDto>> response = dvdService.handleGetAllDvds();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<DvdDto> body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(dto, body.get(0));
        verify(userContextLogger).logUserOperation("GET_ALL_DVDS", "Fetching all DVDs");
    }

    @Test
    void handleGetDvdById_success() {
        String id = "1";
        Dvd dvd = mock(Dvd.class);
        DvdDto enhanced = mock(DvdDto.class);
        when(dvdRepository.findById(1L)).thenReturn(Optional.of(dvd));
        when(dvdMapper.toEnhancedDto(dvd)).thenReturn(enhanced);

        ResponseEntity<DvdDto> response = dvdService.handleGetDvdById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(enhanced, response.getBody());
        verify(userContextLogger).logUserOperation("GET_DVD_BY_ID", "Fetching DVD with ID: 1");
    }

    @Test
    void handleGetDvdById_badRequest_forNonNumericId() {
        ResponseEntity<DvdDto> response = dvdService.handleGetDvdById("abc");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userContextLogger).logUserOperation("GET_DVD_BY_ID", "Invalid ID format: abc");
    }

    @Test
    void handleGetDvdById_notFound_whenMissing() {
        when(dvdRepository.findById(1L)).thenReturn(Optional.empty());
        ResponseEntity<DvdDto> response = dvdService.handleGetDvdById("1");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleCreateDvd_success_withPosterProcessing() throws Exception {
        DvdDto dto = mock(DvdDto.class);
        when(dto.getTitle()).thenReturn("Test Title");
        when(dto.getPosterImage()).thenReturn("base64data");
        when(resourceService.savePosterImage("base64data")).thenReturn("saved.jpg");
        when(resourceService.generatePosterUrl("saved.jpg")).thenReturn("http://host/saved.jpg");
        Dvd dvd = mock(Dvd.class);
        when(dvdMapper.toDvd(dto)).thenReturn(dvd);

        ResponseEntity<Void> response = dvdService.handleCreateDvd(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(validationService).validateForCreation(dto);
        verify(resourceService).savePosterImage("base64data");
        verify(resourceService).generatePosterUrl("saved.jpg");
        verify(dto).setPosterUrl("http://host/saved.jpg");
        verify(dvdRepository).save(dvd);
        verify(userContextLogger).logUserOperation("CREATE_DVD", "Successfully created DVD");
    }

    @Test
    void handleCreateDvd_validationFails_returnsUnprocessableEntity() throws Exception {
        DvdDto dto = mock(DvdDto.class);
        when(dto.getTitle()).thenReturn("Test");
        doThrow(new ValidationException("invalid")).when(validationService).validateForCreation(dto);

        ResponseEntity<Void> response = dvdService.handleCreateDvd(dto);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        verify(userContextLogger).logUserOperation("CREATE_DVD", "Validation failed: invalid");
    }

    @Test
    void handleUpdateDvd_success() throws Exception {
        String id = "2";
        DvdDto dto = mock(DvdDto.class);
        Dvd existing = mock(Dvd.class);
        when(dvdRepository.findById(2L)).thenReturn(Optional.of(existing));

        ResponseEntity<Void> response = dvdService.handleUpdateDvd(id, dto);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(validationService).validateForUpdate(dto);
        verify(updateService).applyUpdates(existing, dto);
        verify(dvdRepository).save(existing);
        verify(userContextLogger).logUserOperation("UPDATE_DVD", "Successfully updated DVD");
    }

    @Test
    void handleUpdateDvd_badRequest_forNonNumericId() {
        DvdDto dto = mock(DvdDto.class);
        ResponseEntity<Void> response = dvdService.handleUpdateDvd("x", dto);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleUpdateDvd_notFound_whenMissing() throws Exception {
        when(dvdRepository.findById(5L)).thenReturn(Optional.empty());
        DvdDto dto = mock(DvdDto.class);
        ResponseEntity<Void> response = dvdService.handleUpdateDvd("5", dto);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleUpdateDvd_validationFails_returnsUnprocessableEntity() throws Exception {
        DvdDto dto = mock(DvdDto.class);
        doThrow(new ValidationException("bad")).when(validationService).validateForUpdate(dto);
        ResponseEntity<Void> response = dvdService.handleUpdateDvd("1", dto);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }

    @Test
    void handleGetAllDvdsWithOptionalFilters_noFilters_callsHandleGetAllDvds() {
        when(dvdRepository.findAll()).thenReturn(Arrays.asList(new Dvd(), new Dvd()));
        when(dvdMapper.toDto(org.mockito.ArgumentMatchers.any(Dvd.class))).thenReturn(new DvdDto());
        ResponseEntity<List<DvdDto>> result = dvdService.handleGetAllDvdsWithOptionalFilters(null, null, null);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void handleGetAllDvdsWithOptionalFilters_withFilters_callsHandleGetFilteredDvds() {
        String searchPhrase = "test";
        List<String> genreNames = Arrays.asList("Action");
        List<Long> genreIds = Arrays.asList(1L);

        when(dvdFilterMapper.mapToFilterDto(searchPhrase, genreNames, genreIds))
                .thenReturn(mock(pl.kielce.tu.backend.model.dto.DvdFilterDto.class));
        when(dvdRepository.findAll()).thenReturn(Arrays.asList(new Dvd()));
        when(dvdFilterMapper.hasAnyFilter(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        when(dvdFilterService.applyFilters(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(Arrays.asList(new Dvd()));
        when(dvdMapper.toDto(org.mockito.ArgumentMatchers.any(Dvd.class))).thenReturn(new DvdDto());

        ResponseEntity<List<DvdDto>> result = dvdService.handleGetAllDvdsWithOptionalFilters(searchPhrase, genreNames,
                genreIds);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }
}
