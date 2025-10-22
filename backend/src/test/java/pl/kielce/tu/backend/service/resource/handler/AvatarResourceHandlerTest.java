package pl.kielce.tu.backend.service.resource.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class AvatarResourceHandlerTest {

    @Mock
    private UserContextLogger userContextLogger;

    private AvatarResourceHandler avatarHandler;
    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        avatarHandler = new AvatarResourceHandler(userContextLogger);
        tempDir = Files.createTempDirectory("avatars-test");
        ReflectionTestUtils.setField(avatarHandler, "avatarDirectory", tempDir.toString());
        ReflectionTestUtils.setField(avatarHandler, "cacheControlHeader", "public, max-age=31536000");
        ReflectionTestUtils.setField(avatarHandler, "defaultContentType", "application/octet-stream");
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

    @Test
    void handleGetRequest_existingFile_returnsOkResponse() throws Exception {
        String filename = "test-avatar.png";
        byte[] content = new byte[] { 1, 2, 3, 4, 5 };
        Files.write(tempDir.resolve(filename), content);

        ResponseEntity<Resource> response = avatarHandler.handleGetRequest(filename);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userContextLogger, times(1)).logUserOperation("GET_AVATAR", "Retrieving: " + filename);
    }

    @Test
    void handleGetRequest_nonExistentFile_returnsNotFound() {
        String filename = "nonexistent-avatar.png";

        ResponseEntity<Resource> response = avatarHandler.handleGetRequest(filename);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleGetRequest_pathTraversalAttempt_returnsError() {
        String maliciousFilename = "../../../etc/passwd";

        ResponseEntity<Resource> response = avatarHandler.handleGetRequest(maliciousFilename);

        assertEquals(true, response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
    }

    @Test
    void handleGetRequest_pathTraversalWithDots_returnsError() {
        String maliciousFilename = "..%2F..%2Fetc%2Fpasswd";

        ResponseEntity<Resource> response = avatarHandler.handleGetRequest(maliciousFilename);

        assertEquals(true, response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
    }

    @Test
    void handleGetRequest_setsCorrectHeaders() throws Exception {
        String filename = "avatar-with-headers.png";
        byte[] content = new byte[] { 10, 20, 30 };
        Files.write(tempDir.resolve(filename), content);

        ResponseEntity<Resource> response = avatarHandler.handleGetRequest(filename);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getContentType());
        assertNotNull(response.getHeaders().getETag());
        assertNotNull(response.getHeaders().getCacheControl());
        assertEquals(content.length, response.getHeaders().getContentLength());
    }

    @Test
    void handleGetRequest_setsInlineContentDisposition() throws Exception {
        String filename = "avatar-disposition.png";
        Files.write(tempDir.resolve(filename), new byte[] { 1, 2, 3 });

        ResponseEntity<Resource> response = avatarHandler.handleGetRequest(filename);

        String disposition = response.getHeaders().getFirst("Content-Disposition");
        assertNotNull(disposition);
        assertEquals(true, disposition.contains("inline"));
        assertEquals(true, disposition.contains(filename));
    }

    @Test
    void handleGetRequest_nullFilename_returnsError() {
        ResponseEntity<Resource> response = avatarHandler.handleGetRequest(null);

        assertEquals(true, response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
    }

    @Test
    void handleGetRequest_emptyFilename_returnsError() {
        ResponseEntity<Resource> response = avatarHandler.handleGetRequest("");

        assertEquals(true, response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
    }

    @Test
    void handleGetRequest_detectsImageContentType() throws Exception {
        String[] testCases = {
                "avatar.png",
                "avatar.jpg",
                "avatar.jpeg"
        };

        for (String filename : testCases) {
            Files.write(tempDir.resolve(filename), new byte[] { 1, 2, 3 });

            ResponseEntity<Resource> response = avatarHandler.handleGetRequest(filename);
            HttpHeaders responseHeaders = response.getHeaders();
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(responseHeaders);
            MediaType responseMediaType = responseHeaders.getContentType();
            assertNotNull(responseMediaType);
            String contentType = responseMediaType.toString();
            assertEquals(true, contentType.startsWith("image/"),
                    "Expected image content type for " + filename + " but got " + contentType);
        }
    }

    @Test
    void handleGetRequest_cacheableResponse() throws Exception {
        String filename = "cacheable-avatar.png";
        Files.write(tempDir.resolve(filename), new byte[] { 5, 10, 15 });

        ResponseEntity<Resource> response = avatarHandler.handleGetRequest(filename);

        String cacheControl = response.getHeaders().getCacheControl();
        assertNotNull(cacheControl);
        assertEquals(true, cacheControl.contains("max-age"));
    }

    @Test
    void handleGetRequest_multipleRequests_logsEachRequest() throws Exception {
        String filename = "logged-avatar.png";
        Files.write(tempDir.resolve(filename), new byte[] { 1 });

        avatarHandler.handleGetRequest(filename);
        avatarHandler.handleGetRequest(filename);
        avatarHandler.handleGetRequest(filename);

        verify(userContextLogger, times(3)).logUserOperation("GET_AVATAR", "Retrieving: " + filename);
    }
}
