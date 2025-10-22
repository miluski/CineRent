package pl.kielce.tu.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import org.springframework.http.MediaType;
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

    @Test
    void downloadPoster_withDifferentFilename_shouldPassCorrectParameter() {
        String filename = "movie_poster_456.webp";
        Resource resource = new ByteArrayResource(new byte[] { 4, 5, 6 });
        ResponseEntity<Resource> expected = ResponseEntity.ok(resource);

        when(resourceService.handleGetPosterRequest(filename)).thenReturn(expected);

        resourceController.downloadPoster(filename);

        verify(resourceService).handleGetPosterRequest(eq(filename));
        verify(resourceService, never()).handleGetPosterRequest(argThat(arg -> !filename.equals(arg)));
    }

    @Test
    void downloadPoster_shouldReturnExactResponseFromService() {
        String filename = "test.png";
        Resource resource = new ByteArrayResource(new byte[] { 7, 8, 9 });
        ResponseEntity<Resource> customResponse = ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);

        when(resourceService.handleGetPosterRequest(filename)).thenReturn(customResponse);

        ResponseEntity<Resource> response = resourceController.downloadPoster(filename);

        assertThat(response).isSameAs(customResponse);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_PNG);
    }

    @Test
    void downloadAvatar_shouldDelegateToResourceService() {
        String filename = "user_123_avatar.png";
        Resource resource = new ByteArrayResource(new byte[] { 10, 11, 12 });
        ResponseEntity<Resource> expected = ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);

        when(resourceService.handleGetAvatarRequest(filename)).thenReturn(expected);

        ResponseEntity<Resource> response = resourceController.downloadAvatar(filename);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(resource);
        verify(resourceService, times(1)).handleGetAvatarRequest(filename);
    }

    @Test
    void downloadAvatar_withDifferentFilename_shouldPassCorrectParameter() {
        String filename = "user_999_avatar.png";
        Resource resource = new ByteArrayResource(new byte[] { 13, 14, 15 });
        ResponseEntity<Resource> expected = ResponseEntity.ok().body(resource);

        when(resourceService.handleGetAvatarRequest(filename)).thenReturn(expected);

        resourceController.downloadAvatar(filename);

        verify(resourceService).handleGetAvatarRequest(eq(filename));
        verify(resourceService, never()).handleGetAvatarRequest(argThat(arg -> !filename.equals(arg)));
    }

    @Test
    void downloadAvatar_whenServiceReturnsNotFound_shouldReturnNotFound() {
        String filename = "nonexistent_avatar.png";
        ResponseEntity<Resource> notFoundResponse = ResponseEntity.notFound().build();

        when(resourceService.handleGetAvatarRequest(filename)).thenReturn(notFoundResponse);

        ResponseEntity<Resource> response = resourceController.downloadAvatar(filename);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void downloadAvatar_shouldReturnExactResponseFromService() {
        String filename = "avatar.png";
        Resource resource = new ByteArrayResource(new byte[] { 16, 17, 18 });
        ResponseEntity<Resource> customResponse = ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.IMAGE_PNG)
                .header("Cache-Control", "max-age=3600")
                .body(resource);

        when(resourceService.handleGetAvatarRequest(filename)).thenReturn(customResponse);

        ResponseEntity<Resource> response = resourceController.downloadAvatar(filename);

        assertThat(response).isSameAs(customResponse);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_PNG);
        assertThat(response.getHeaders().getFirst("Cache-Control")).isEqualTo("max-age=3600");
    }

    @Test
    void downloadPoster_shouldNotCallAvatarMethod() {
        String filename = "poster.jpg";
        Resource resource = new ByteArrayResource(new byte[] { 19, 20, 21 });
        ResponseEntity<Resource> expected = ResponseEntity.ok(resource);

        when(resourceService.handleGetPosterRequest(filename)).thenReturn(expected);

        resourceController.downloadPoster(filename);

        verify(resourceService, times(1)).handleGetPosterRequest(filename);
        verify(resourceService, never()).handleGetAvatarRequest(anyString());
    }

    @Test
    void downloadAvatar_shouldNotCallPosterMethod() {
        String filename = "avatar.png";
        Resource resource = new ByteArrayResource(new byte[] { 22, 23, 24 });
        ResponseEntity<Resource> expected = ResponseEntity.ok().body(resource);

        when(resourceService.handleGetAvatarRequest(filename)).thenReturn(expected);

        resourceController.downloadAvatar(filename);

        verify(resourceService, times(1)).handleGetAvatarRequest(filename);
        verify(resourceService, never()).handleGetPosterRequest(anyString());
    }

    @Test
    void downloadPoster_withEmptyFilename_shouldStillDelegate() {
        String filename = "";
        Resource resource = new ByteArrayResource(new byte[] { 25, 26, 27 });
        ResponseEntity<Resource> expected = ResponseEntity.ok(resource);

        when(resourceService.handleGetPosterRequest(filename)).thenReturn(expected);

        resourceController.downloadPoster(filename);

        verify(resourceService).handleGetPosterRequest("");
    }

    @Test
    void downloadAvatar_withEmptyFilename_shouldStillDelegate() {
        String filename = "";
        Resource resource = new ByteArrayResource(new byte[] { 28, 29, 30 });
        ResponseEntity<Resource> expected = ResponseEntity.ok().body(resource);

        when(resourceService.handleGetAvatarRequest(filename)).thenReturn(expected);

        resourceController.downloadAvatar(filename);

        verify(resourceService).handleGetAvatarRequest("");
    }
}
