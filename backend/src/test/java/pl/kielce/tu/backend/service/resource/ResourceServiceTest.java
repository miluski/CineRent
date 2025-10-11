package pl.kielce.tu.backend.service.resource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private UserContextLogger userContextLogger;

    private ResourceService resourceService;
    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        resourceService = new ResourceService(userContextLogger);
        tempDir = Files.createTempDirectory("posters-test");
        setPrivateField(resourceService, "posterDirectory", tempDir.toString());
        setPrivateField(resourceService, "posterBaseUrl", "/test/base-url");
        setPrivateField(resourceService, "maxPosterSize", 5242880L);
        setPrivateField(resourceService, "cacheControlHeader", "public, max-age=31536000");
        setPrivateField(resourceService, "defaultContentType", "application/octet-stream");

        invokePostConstruct(resourceService);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .map(Path::toFile)
                    .forEach(f -> {
                        if (!f.delete()) {
                            f.deleteOnExit();
                        }
                    });
        }
    }

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static void invokePostConstruct(Object target) throws Exception {
        var method = target.getClass().getDeclaredMethod("initializePosterDirectory");
        method.setAccessible(true);
        method.invoke(target);
    }

    @Test
    void savePosterImage_validBase64_savesFileAndReturnsFilename() throws Exception {
        byte[] imageBytes = new byte[] { 1, 2, 3, 4, 5 };
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String dataUrl = "data:image/png;base64," + base64;

        String filename = resourceService.savePosterImage(dataUrl);

        assertNotNull(filename);
        assertTrue(filename.endsWith(".png"));
        Path saved = tempDir.resolve(filename);
        assertTrue(Files.exists(saved));
        byte[] savedBytes = Files.readAllBytes(saved);
        assertArrayEquals(imageBytes, savedBytes);
    }

    @Test
    void handleGetPosterRequest_existingFile_returnsOkAndHeaders() throws Exception {
        String filename = "sample.png";
        byte[] content = new byte[] { 10, 11, 12, 13 };
        Path file = tempDir.resolve(filename);
        Files.write(file, content);

        ResponseEntity<Resource> response = resourceService.handleGetPosterRequest(filename);

        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        var contentType = response.getHeaders().getContentType();
        assertNotNull(contentType);
        assertTrue(contentType.toString().contains("image"));
        assertEquals(content.length, response.getHeaders().getContentLength());
        String etag = response.getHeaders().getETag();
        assertNotNull(etag);
        assertTrue(!etag.isEmpty());
        String cacheControl = response.getHeaders().getCacheControl();
        assertNotNull(cacheControl);
        assertTrue(cacheControl.contains("max-age"));
        String disposition = response.getHeaders().getFirst("Content-Disposition");
        assertNotNull(disposition);
        assertTrue(disposition.contains("inline"));
    }

    @Test
    void handleGetPosterRequest_missingFile_returnsNotFound() {
        ResponseEntity<Resource> response = resourceService.handleGetPosterRequest("nonexistent.png");
        assertEquals(true, response.getStatusCode().is4xxClientError());
    }

    @Test
    void savePosterImage_invalidInput_throwsValidationException() {
        assertThrows(ValidationException.class, () -> resourceService.savePosterImage("not-a-data-url"));
        assertThrows(ValidationException.class, () -> resourceService.savePosterImage(null));
    }

    @Test
    void generatePosterUrl_behaviour() {
        String filename = "a.png";
        String url = resourceService.generatePosterUrl(filename);
        assertEquals("/test/base-url/" + filename, url);

        assertNull(resourceService.generatePosterUrl(null));
        assertNull(resourceService.generatePosterUrl("   "));
    }
}
