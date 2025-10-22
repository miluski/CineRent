package pl.kielce.tu.backend.service.resource.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.service.resource.validation.ResourceValidationService;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class PosterResourceHandlerTest {

    @Mock
    private UserContextLogger userContextLogger;

    @Mock
    private ResourceValidationService validationService;

    @InjectMocks
    private PosterResourceHandler posterResourceHandler;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(posterResourceHandler, "posterDirectory", tempDir.toString());
        ReflectionTestUtils.setField(posterResourceHandler, "posterBaseUrl",
                "https://localhost/api/v1/resources/posters");
        ReflectionTestUtils.setField(posterResourceHandler, "cacheControlHeader", "max-age=31536000, public");
        ReflectionTestUtils.setField(posterResourceHandler, "defaultContentType", "application/octet-stream");
    }

    @Test
    void initializeDirectory_shouldCreateDirectoryIfNotExists() throws IOException {

        Path newDir = tempDir.resolve("new-posters");
        ReflectionTestUtils.setField(posterResourceHandler, "posterDirectory", newDir.toString());

        posterResourceHandler.initializeDirectory();

        assertThat(Files.exists(newDir)).isTrue();
        assertThat(Files.isDirectory(newDir)).isTrue();
        verify(userContextLogger).logUserOperation(eq("INIT"), contains("Created poster directory"));
    }

    @Test
    void initializeDirectory_shouldNotFailIfDirectoryExists() {

        posterResourceHandler.initializeDirectory();

        verify(userContextLogger).logUserOperation(eq("INIT"), anyString());
    }

    @Test
    void handleGetRequest_shouldReturnPosterWhenExists() throws IOException {

        String filename = "test-poster.jpg";
        byte[] imageData = new byte[] { 1, 2, 3, 4, 5 };
        Path posterPath = tempDir.resolve(filename);
        Files.write(posterPath, imageData);

        ResponseEntity<Resource> response = posterResourceHandler.handleGetRequest(filename);
        Resource body = response.getBody();
        assertNotNull(body);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body.exists()).isTrue();
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentLength()).isEqualTo(imageData.length);
        verify(userContextLogger).logUserOperation(eq("GET_POSTER"), contains("Retrieving"));
    }

    @Test
    void handleGetRequest_shouldReturnNotFoundWhenPosterDoesNotExist() {

        String filename = "nonexistent-poster.jpg";

        ResponseEntity<Resource> response = posterResourceHandler.handleGetRequest(filename);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
        verify(userContextLogger).logUserOperation(eq("GET_POSTER"), contains("Not found"));
    }

    @Test
    void handleGetRequest_shouldIncludeCacheControlHeader() throws IOException {

        String filename = "cached-poster.jpg";
        Path posterPath = tempDir.resolve(filename);
        Files.write(posterPath, new byte[] { 1, 2, 3 });

        ResponseEntity<Resource> response = posterResourceHandler.handleGetRequest(filename);

        assertThat(response.getHeaders().getCacheControl()).isEqualTo("max-age=31536000, public");
    }

    @Test
    void handleGetRequest_shouldIncludeETagHeader() throws IOException {

        String filename = "etag-poster.jpg";
        Path posterPath = tempDir.resolve(filename);
        Files.write(posterPath, new byte[] { 1, 2, 3 });

        ResponseEntity<Resource> response = posterResourceHandler.handleGetRequest(filename);

        assertThat(response.getHeaders().getETag()).isNotNull();
        assertThat(response.getHeaders().getETag()).contains(filename);
    }

    @Test
    void handleGetRequest_shouldIncludeContentDispositionHeader() throws IOException {

        String filename = "download-poster.jpg";
        Path posterPath = tempDir.resolve(filename);
        Files.write(posterPath, new byte[] { 1, 2, 3 });

        ResponseEntity<Resource> response = posterResourceHandler.handleGetRequest(filename);

        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("inline")
                .contains(filename);
    }

    @Test
    void handleGetRequest_shouldPreventPathTraversal() {

        String maliciousFilename = "../../../etc/passwd";

        ResponseEntity<Resource> response = posterResourceHandler.handleGetRequest(maliciousFilename);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(userContextLogger).logUserOperation(eq("GET_POSTER"), contains("Error"));
    }

    @Test
    void handleGetRequest_shouldPreventPathTraversalWithEncodedSlash() {

        String maliciousFilename = "..%2F..%2Fetc%2Fpasswd";

        ResponseEntity<Resource> response = posterResourceHandler.handleGetRequest(maliciousFilename);

        assertThat(response.getStatusCode()).isIn(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.NOT_FOUND);
    }

    @Test
    void saveImage_shouldSaveValidBase64Image() throws ValidationException, IOException {

        byte[] imageBytes = new byte[] { 1, 2, 3, 4, 5 };
        String base64Data = Base64.getEncoder().encodeToString(imageBytes);
        String base64Image = "data:image/jpeg;base64," + base64Data;

        doNothing().when(validationService).validateBase64Image(base64Image);
        when(validationService.parseBase64Image(base64Image))
                .thenReturn(new String[] { "data:image/jpeg", base64Data });
        when(validationService.extractMimeType("data:image/jpeg")).thenReturn("image/jpeg");
        when(validationService.getExtensionFromMimeType("image/jpeg")).thenReturn("jpg");
        doNothing().when(validationService).validateImageSize(imageBytes);

        String filename = posterResourceHandler.saveImage(base64Image);

        assertThat(filename).isNotNull();
        assertThat(filename).endsWith(".jpg");
        assertThat(Files.exists(tempDir.resolve(filename))).isTrue();
        verify(validationService).validateBase64Image(base64Image);
        verify(validationService).parseBase64Image(base64Image);
        verify(validationService).validateImageSize(imageBytes);
        verify(userContextLogger).logUserOperation(eq("SAVE_POSTER"), contains("Saved poster"));
    }

    @Test
    void saveImage_shouldThrowValidationExceptionWhenInvalidBase64() throws ValidationException {

        String invalidBase64 = "invalid-base64-data";
        doThrow(new ValidationException("Invalid base64 format"))
                .when(validationService).validateBase64Image(invalidBase64);

        assertThatThrownBy(() -> posterResourceHandler.saveImage(invalidBase64))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid base64 format");
    }

    @Test
    void saveImage_shouldThrowValidationExceptionWhenImageTooLarge() throws ValidationException {

        byte[] largeImageBytes = new byte[10 * 1024 * 1024]; // 10MB
        String base64Data = Base64.getEncoder().encodeToString(largeImageBytes);
        String base64Image = "data:image/jpeg;base64," + base64Data;

        doNothing().when(validationService).validateBase64Image(base64Image);
        when(validationService.parseBase64Image(base64Image))
                .thenReturn(new String[] { "data:image/jpeg", base64Data });
        when(validationService.extractMimeType("data:image/jpeg")).thenReturn("image/jpeg");
        when(validationService.getExtensionFromMimeType("image/jpeg")).thenReturn("jpg");
        doThrow(new ValidationException("Image size exceeds maximum allowed"))
                .when(validationService).validateImageSize(largeImageBytes);

        assertThatThrownBy(() -> posterResourceHandler.saveImage(base64Image))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Image size exceeds maximum allowed");
    }

    @Test
    void saveImage_shouldSupportPngFormat() throws ValidationException, IOException {

        byte[] imageBytes = new byte[] { 1, 2, 3 };
        String base64Data = Base64.getEncoder().encodeToString(imageBytes);
        String base64Image = "data:image/png;base64," + base64Data;

        doNothing().when(validationService).validateBase64Image(base64Image);
        when(validationService.parseBase64Image(base64Image))
                .thenReturn(new String[] { "data:image/png", base64Data });
        when(validationService.extractMimeType("data:image/png")).thenReturn("image/png");
        when(validationService.getExtensionFromMimeType("image/png")).thenReturn("png");
        doNothing().when(validationService).validateImageSize(imageBytes);

        String filename = posterResourceHandler.saveImage(base64Image);

        assertThat(filename).endsWith(".png");
        assertThat(Files.exists(tempDir.resolve(filename))).isTrue();
    }

    @Test
    void saveImage_shouldSupportWebpFormat() throws ValidationException, IOException {

        byte[] imageBytes = new byte[] { 1, 2, 3 };
        String base64Data = Base64.getEncoder().encodeToString(imageBytes);
        String base64Image = "data:image/webp;base64," + base64Data;

        doNothing().when(validationService).validateBase64Image(base64Image);
        when(validationService.parseBase64Image(base64Image))
                .thenReturn(new String[] { "data:image/webp", base64Data });
        when(validationService.extractMimeType("data:image/webp")).thenReturn("image/webp");
        when(validationService.getExtensionFromMimeType("image/webp")).thenReturn("webp");
        doNothing().when(validationService).validateImageSize(imageBytes);

        String filename = posterResourceHandler.saveImage(base64Image);

        assertThat(filename).endsWith(".webp");
        assertThat(Files.exists(tempDir.resolve(filename))).isTrue();
    }

    @Test
    void generateUrl_shouldReturnCorrectUrl() {

        String filename = "poster123.jpg";

        String url = posterResourceHandler.generateUrl(filename);

        assertThat(url).isEqualTo("https://localhost/api/v1/resources/posters/poster123.jpg");
    }

    @Test
    void generateUrl_shouldReturnNullForNullFilename() {

        String url = posterResourceHandler.generateUrl(null);

        assertThat(url).isNull();
    }

    @Test
    void generateUrl_shouldReturnNullForEmptyFilename() {

        String url = posterResourceHandler.generateUrl("");

        assertThat(url).isNull();
    }

    @Test
    void generateUrl_shouldReturnNullForWhitespaceFilename() {

        String url = posterResourceHandler.generateUrl("   ");

        assertThat(url).isNull();
    }

    @Test
    void handleGetRequest_shouldSetCorrectContentTypeForJpeg() throws IOException {

        String filename = "test.jpeg";
        Path posterPath = tempDir.resolve(filename);
        Files.write(posterPath, new byte[] { 1, 2, 3 });

        ResponseEntity<Resource> response = posterResourceHandler.handleGetRequest(filename);

        assertThat(response.getHeaders().getContentType()).isIn(
                MediaType.IMAGE_JPEG,
                MediaType.parseMediaType("application/octet-stream"));
    }

    @Test
    void handleGetRequest_shouldSetCorrectContentTypeForPng() throws IOException {

        String filename = "test.png";
        Path posterPath = tempDir.resolve(filename);
        Files.write(posterPath, new byte[] { 1, 2, 3 });

        ResponseEntity<Resource> response = posterResourceHandler.handleGetRequest(filename);

        assertThat(response.getHeaders().getContentType()).isIn(
                MediaType.IMAGE_PNG,
                MediaType.parseMediaType("application/octet-stream"));
    }

    @Test
    void saveImage_shouldGenerateUniqueFilenamesForMultipleSaves() throws ValidationException, IOException {

        byte[] imageBytes = new byte[] { 1, 2, 3 };
        String base64Data = Base64.getEncoder().encodeToString(imageBytes);
        String base64Image = "data:image/jpeg;base64," + base64Data;

        doNothing().when(validationService).validateBase64Image(base64Image);
        when(validationService.parseBase64Image(base64Image))
                .thenReturn(new String[] { "data:image/jpeg", base64Data });
        when(validationService.extractMimeType("data:image/jpeg")).thenReturn("image/jpeg");
        when(validationService.getExtensionFromMimeType("image/jpeg")).thenReturn("jpg");
        doNothing().when(validationService).validateImageSize(imageBytes);

        String filename1 = posterResourceHandler.saveImage(base64Image);
        String filename2 = posterResourceHandler.saveImage(base64Image);

        assertThat(filename1).isNotEqualTo(filename2);
        assertThat(Files.exists(tempDir.resolve(filename1))).isTrue();
        assertThat(Files.exists(tempDir.resolve(filename2))).isTrue();
    }

}
