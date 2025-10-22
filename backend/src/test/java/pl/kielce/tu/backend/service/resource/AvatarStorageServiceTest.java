package pl.kielce.tu.backend.service.resource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import pl.kielce.tu.backend.exception.FileStorageException;
import pl.kielce.tu.backend.service.avatar.AvatarStorageService;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class AvatarStorageServiceTest {

    @Mock
    private UserContextLogger userContextLogger;

    private AvatarStorageService avatarStorageService;
    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        avatarStorageService = new AvatarStorageService(userContextLogger);
        tempDir = Files.createTempDirectory("avatars-test");
        ReflectionTestUtils.setField(avatarStorageService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(avatarStorageService, "maxFileSize", 5242880L);
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
    void storeAvatar_validBase64_savesFileAndReturnsPath() throws Exception {
        byte[] imageBytes = new byte[] { 1, 2, 3, 4, 5 };
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String dataUrl = "data:image/png;base64," + base64;

        String avatarPath = avatarStorageService.storeAvatar(dataUrl, 1L);

        assertNotNull(avatarPath);
        assertTrue(avatarPath.startsWith("/resources/avatars/user_1_"));
        assertTrue(avatarPath.endsWith(".png"));
    }

    @Test
    void storeAvatar_nullInput_throwsFileStorageException() {
        assertThrows(FileStorageException.class, () -> avatarStorageService.storeAvatar(null, 1L));
    }

    @Test
    void storeAvatar_emptyInput_throwsFileStorageException() {
        assertThrows(FileStorageException.class, () -> avatarStorageService.storeAvatar("", 1L));
    }

    @Test
    void storeAvatar_fileSizeExceedsLimit_throwsFileStorageException() {
        byte[] largeImage = new byte[6 * 1024 * 1024];
        String base64 = Base64.getEncoder().encodeToString(largeImage);
        String dataUrl = "data:image/jpeg;base64," + base64;

        assertThrows(FileStorageException.class, () -> avatarStorageService.storeAvatar(dataUrl, 1L));
    }

    @Test
    void storeAvatar_differentImageFormats_savesAsPng() throws Exception {
        String[] mimeTypes = {
                "data:image/png;base64,",
                "data:image/jpeg;base64,",
                "data:image/gif;base64,"
        };

        for (String mimePrefix : mimeTypes) {
            byte[] imageBytes = new byte[] { 10, 20, 30 };
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            String dataUrl = mimePrefix + base64;

            String avatarPath = avatarStorageService.storeAvatar(dataUrl, 1L);

            assertTrue(avatarPath.endsWith(".png"),
                    "All formats should be saved as PNG, got: " + avatarPath);
        }
    }

    @Test
    void deleteAvatar_existingFile_deletesSuccessfully() throws Exception {
        byte[] imageBytes = new byte[] { 5, 10, 15 };
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String dataUrl = "data:image/png;base64," + base64;

        String avatarPath = avatarStorageService.storeAvatar(dataUrl, 1L);
        String filename = avatarPath.substring(avatarPath.lastIndexOf("/") + 1);
        Path savedFile = tempDir.resolve(filename);

        assertTrue(Files.exists(savedFile));

        avatarStorageService.deleteAvatar(avatarPath);

        assertTrue(!Files.exists(savedFile));
    }

    @Test
    void deleteAvatar_nonExistentFile_doesNotThrowException() {
        String nonExistentPath = "/resources/avatars/avatar-nonexistent-12345.png";

        avatarStorageService.deleteAvatar(nonExistentPath);
    }

    @Test
    void deleteAvatar_nullPath_doesNotThrowException() {
        avatarStorageService.deleteAvatar(null);
    }

    @Test
    void deleteAvatar_emptyPath_doesNotThrowException() {
        avatarStorageService.deleteAvatar("");
    }

    @Test
    void storeAvatar_generatesUniqueFilenames() throws Exception {
        byte[] imageBytes = new byte[] { 1, 2, 3 };
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String dataUrl = "data:image/png;base64," + base64;

        String path1 = avatarStorageService.storeAvatar(dataUrl, 1L);
        String path2 = avatarStorageService.storeAvatar(dataUrl, 1L);

        assertTrue(!path1.equals(path2), "Avatar paths should be unique");
    }

    @Test
    void storeAvatar_webpFormat_savesAsPng() throws Exception {
        byte[] imageBytes = new byte[] { 7, 14, 21 };
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String dataUrl = "data:image/webp;base64," + base64;

        String avatarPath = avatarStorageService.storeAvatar(dataUrl, 1L);

        assertTrue(avatarPath.endsWith(".png"));
    }

    @Test
    void storeAvatar_invalidBase64Characters_throwsFileStorageException() {
        String invalidDataUrl = "data:image/png;base64,!!!invalid!!!";

        assertThrows(FileStorageException.class, () -> avatarStorageService.storeAvatar(invalidDataUrl, 1L));
    }
}
