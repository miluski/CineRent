package pl.kielce.tu.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import pl.kielce.tu.backend.service.resource.ResourceService;

@ExtendWith(MockitoExtension.class)
class ResourceControllerTest {

    @Mock
    private ResourceService resourceService;

    @InjectMocks
    private ResourceController resourceController;

    @Test
    void downloadPoster_returnsOkWithResource() {
        String filename = "poster.jpg";
        Resource resource = new ByteArrayResource(new byte[] { 1, 2, 3 });
        ResponseEntity<Resource> expected = ResponseEntity.ok(resource);

        when(resourceService.handleGetPosterRequest(filename)).thenReturn(expected);

        ResponseEntity<Resource> actual = resourceController.downloadPoster(filename);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertSame(expected.getBody(), actual.getBody());
        verify(resourceService, times(1)).handleGetPosterRequest(filename);
    }

    @Test
    void downloadPoster_returnsNotFound() {
        String filename = "missing.jpg";
        ResponseEntity<Resource> expected = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        when(resourceService.handleGetPosterRequest(filename)).thenReturn(expected);

        ResponseEntity<Resource> actual = resourceController.downloadPoster(filename);

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
        assertNull(actual.getBody());
        verify(resourceService, times(1)).handleGetPosterRequest(filename);
    }
}
