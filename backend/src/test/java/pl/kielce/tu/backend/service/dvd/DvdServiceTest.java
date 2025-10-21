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
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.mapper.DvdFilterMapper;
import pl.kielce.tu.backend.mapper.DvdMapper;
import pl.kielce.tu.backend.mapper.PageMapper;
import pl.kielce.tu.backend.model.dto.DvdDto;
import pl.kielce.tu.backend.model.dto.PagedResponseDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.repository.DvdRepository;
import pl.kielce.tu.backend.repository.specification.DvdSpecification;
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
    private PageMapper pageMapper;
    @Mock
    private DvdSpecification dvdSpecification;

    private DvdService dvdService;

    @BeforeEach
    void setUp() {
        dvdService = new DvdService(dvdMapper, pageMapper, dvdRepository, updateService, dvdFilterMapper,
                resourceService, dvdSpecification, userContextLogger, validationService);
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleGetAllDvds_returnsOkWithDtos() {
        Dvd dvd = mock(Dvd.class);
        DvdDto dto = mock(DvdDto.class);
        Page<Dvd> page = new PageImpl<>(Arrays.asList(dvd));
        when(dvdRepository.findAll(ArgumentMatchers.any(Pageable.class))).thenReturn(page);
        PagedResponseDto<DvdDto> pagedResponse = PagedResponseDto.<DvdDto>builder()
                .content(Arrays.asList(dto))
                .totalElements(1)
                .totalPages(1)
                .currentPage(0)
                .pageSize(20)
                .build();
        PagedResponseDto<DvdDto> mockResponse = (PagedResponseDto<DvdDto>) (PagedResponseDto<?>) pagedResponse;
        when(pageMapper.toPagedResponse(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenAnswer(invocation -> mockResponse);

        ResponseEntity<PagedResponseDto<DvdDto>> response = dvdService.handleGetAllDvds(0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        PagedResponseDto<DvdDto> body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.getContent().size());
        assertEquals(dto, body.getContent().get(0));
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
    @SuppressWarnings("unchecked")
    void handleGetAllDvdsWithOptionalFilters_noFilters_callsHandleGetAllDvds() {
        Dvd dvd1 = new Dvd();
        Dvd dvd2 = new Dvd();
        Page<Dvd> page = new PageImpl<>(Arrays.asList(dvd1, dvd2));
        when(dvdRepository.findAll(ArgumentMatchers.any(Pageable.class))).thenReturn(page);
        PagedResponseDto<DvdDto> mockResponse = (PagedResponseDto<DvdDto>) (PagedResponseDto<?>) PagedResponseDto
                .<DvdDto>builder().build();
        when(pageMapper.toPagedResponse(ArgumentMatchers.any(Page.class),
                ArgumentMatchers.any())).thenReturn(mockResponse);

        ResponseEntity<PagedResponseDto<DvdDto>> result = dvdService.handleGetAllDvdsWithOptionalFilters(null, null,
                null, 0, 20);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleGetAllDvdsWithOptionalFilters_withFilters_callsHandleGetFilteredDvds() {
        String searchPhrase = "test";
        List<String> genreNames = Arrays.asList("Action");
        List<Long> genreIds = Arrays.asList(1L);

        when(dvdFilterMapper.mapToFilterDto(searchPhrase, genreNames, genreIds))
                .thenReturn(mock(pl.kielce.tu.backend.model.dto.DvdFilterDto.class));
        when(dvdSpecification.withFilters(ArgumentMatchers.any()))
                .thenReturn((root, query, cb) -> cb.conjunction());
        when(dvdRepository.findAll(ArgumentMatchers.any(org.springframework.data.jpa.domain.Specification.class),
                ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(new Dvd())));
        PagedResponseDto<DvdDto> mockResponse = (PagedResponseDto<DvdDto>) (PagedResponseDto<?>) PagedResponseDto
                .<DvdDto>builder().build();
        when(pageMapper.toPagedResponse(ArgumentMatchers.any(Page.class),
                ArgumentMatchers.any())).thenReturn(mockResponse);

        ResponseEntity<PagedResponseDto<DvdDto>> result = dvdService.handleGetAllDvdsWithOptionalFilters(searchPhrase,
                genreNames, genreIds, 0, 20);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }
}
