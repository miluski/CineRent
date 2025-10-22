package pl.kielce.tu.backend.service.avatar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.FileStorageException;
import pl.kielce.tu.backend.util.UserContextLogger;

@Service
@RequiredArgsConstructor
public class AvatarStorageService {

    @Value("${media.avatar.upload-dir:/app/uploads/avatars}")
    private String uploadDir;

    @Value("${media.avatar.max-size:5242880}")
    private long maxFileSize;

    private final UserContextLogger userContextLogger;

    public String storeAvatar(String base64Avatar, Long userId) throws FileStorageException {
        validateBase64Avatar(base64Avatar);
        byte[] decodedBytes = decodeBase64(base64Avatar);
        validateFileSize(decodedBytes);
        String filename = generateUniqueFilename(userId);
        saveToFileSystem(decodedBytes, filename);
        userContextLogger.logUserOperation("AVATAR_UPLOAD", "Avatar stored with filename: " + filename);
        return "/resources/avatars/" + filename;
    }

    public void deleteAvatar(String avatarPath) {
        if (avatarPath == null || avatarPath.isEmpty()) {
            return;
        }
        try {
            String filename = extractFilenameFromPath(avatarPath);
            Path path = Paths.get(uploadDir, filename);
            Files.deleteIfExists(path);
            userContextLogger.logUserOperation("AVATAR_DELETE", "Avatar deleted: " + filename);
        } catch (IOException e) {
            userContextLogger.logUserOperation("AVATAR_DELETE_FAILED", "Failed to delete avatar: " + avatarPath);
        }
    }

    private void validateBase64Avatar(String base64Avatar) throws FileStorageException {
        if (base64Avatar == null || base64Avatar.isEmpty()) {
            throw new FileStorageException("Base64 avatar data cannot be null or empty");
        }
    }

    private byte[] decodeBase64(String base64Avatar) throws FileStorageException {
        try {
            String base64Data = extractBase64Data(base64Avatar);
            return Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            throw new FileStorageException("Invalid base64 format", e);
        }
    }

    private String extractBase64Data(String base64Avatar) {
        if (base64Avatar.contains(",")) {
            return base64Avatar.split(",")[1];
        }
        return base64Avatar;
    }

    private void validateFileSize(byte[] decodedBytes) throws FileStorageException {
        if (decodedBytes.length > maxFileSize) {
            throw new FileStorageException("File size exceeds maximum allowed size of " + maxFileSize + " bytes");
        }
    }

    private String generateUniqueFilename(Long userId) {
        String uniqueId = UUID.randomUUID().toString();
        return "user_" + userId + "_" + uniqueId + ".png";
    }

    private Path saveToFileSystem(byte[] decodedBytes, String filename) throws FileStorageException {
        try {
            Path uploadPath = Paths.get(uploadDir);
            createDirectoryIfNotExists(uploadPath);
            Path filePath = uploadPath.resolve(filename);
            Files.write(filePath, decodedBytes);
            return filePath;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store avatar file", e);
        }
    }

    private void createDirectoryIfNotExists(Path uploadPath) throws IOException {
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }

    private String extractFilenameFromPath(String avatarPath) {
        return avatarPath.substring(avatarPath.lastIndexOf('/') + 1);
    }

}
