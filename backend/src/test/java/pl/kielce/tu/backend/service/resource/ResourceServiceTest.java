package pl.kielce.tu.backend.service.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import pl.kielce.tu.backend.service.resource.handler.AvatarResourceHandler;
import pl.kielce.tu.backend.service.resource.handler.PosterResourceHandler;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private PosterResourceHandler posterHandler;

    @Mock
    private AvatarResourceHandler avatarHandler;

    @Mock
    private Resource mockResource;

    private ResourceService resourceService;

    @BeforeEach
    void setUp() {
        resourceService = new ResourceService(posterHandler, avatarHandler);
    }

    @Test
    void handleGetPosterRequest_delegatesToPosterHandler() {
        String filename = "test-poster.png";
        ResponseEntity<Resource> expectedResponse = ResponseEntity.ok(mockResource);
        when(posterHandler.handleGetRequest(filename)).thenReturn(expectedResponse);

        ResponseEntity<Resource> result = resourceService.handleGetPosterRequest(filename);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(mockResource, result.getBody());
        verify(posterHandler, times(1)).handleGetRequest(filename);
    }

    @Test
    void handleGetAvatarRequest_delegatesToAvatarHandler() {
        String filename = "test-avatar.jpg";
        ResponseEntity<Resource> expectedResponse = ResponseEntity.ok(mockResource);
        when(avatarHandler.handleGetRequest(filename)).thenReturn(expectedResponse);

        ResponseEntity<Resource> result = resourceService.handleGetAvatarRequest(filename);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(mockResource, result.getBody());
        verify(avatarHandler, times(1)).handleGetRequest(filename);
    }

    @Test
    void savePosterImage_delegatesToPosterHandler() throws Exception {
        String base64Image = "data:image/png;base64,iVBORw0KGgoAAAANS";
        String expectedFilename = "saved-poster-123.png";
        when(posterHandler.saveImage(base64Image)).thenReturn(expectedFilename);

        String result = resourceService.savePosterImage(base64Image);

        assertEquals(expectedFilename, result);
        verify(posterHandler, times(1)).saveImage(base64Image);
    }

    @Test
    void generatePosterUrl_delegatesToPosterHandler() {
        String filename = "poster-456.jpg";
        String expectedUrl = "/api/v1/resources/posters/" + filename;
        when(posterHandler.generateUrl(filename)).thenReturn(expectedUrl);

        String result = resourceService.generatePosterUrl(filename);

        assertEquals(expectedUrl, result);
        verify(posterHandler, times(1)).generateUrl(filename);
    }

    @Test
    void handleGetPosterRequest_returnsNotFoundWhenHandlerReturnsNotFound() {
        String filename = "nonexistent.png";
        ResponseEntity<Resource> notFoundResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        when(posterHandler.handleGetRequest(filename)).thenReturn(notFoundResponse);

        ResponseEntity<Resource> result = resourceService.handleGetPosterRequest(filename);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(posterHandler, times(1)).handleGetRequest(filename);
    }

    @Test
    void handleGetAvatarRequest_returnsNotFoundWhenHandlerReturnsNotFound() {
        String filename = "nonexistent-avatar.jpg";
        ResponseEntity<Resource> notFoundResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        when(avatarHandler.handleGetRequest(filename)).thenReturn(notFoundResponse);

        ResponseEntity<Resource> result = resourceService.handleGetAvatarRequest(filename);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(avatarHandler, times(1)).handleGetRequest(filename);
    }

    @Test
    void savePosterImage_propagatesHandlerResult() throws Exception {
        String base64 = "data:image/jpeg;base64,/9j/4AAQSkZJRg";
        String generatedFilename = "uuid-generated-name.jpeg";
        when(posterHandler.saveImage(base64)).thenReturn(generatedFilename);

        String result = resourceService.savePosterImage(base64);

        assertEquals(generatedFilename, result);
        verify(posterHandler, times(1)).saveImage(base64);
    }

    @Test
    void generatePosterUrl_returnsNullWhenHandlerReturnsNull() {
        when(posterHandler.generateUrl(anyString())).thenReturn(null);

        String result = resourceService.generatePosterUrl("anything.png");

        assertEquals(null, result);
        verify(posterHandler, times(1)).generateUrl(anyString());
    }
}
