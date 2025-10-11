package pl.kielce.tu.backend.service.resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ImageType;
import pl.kielce.tu.backend.util.UserContextLogger;

@Service
@RequiredArgsConstructor
public class ResourceService {

    @Value("${media.poster.max-size}")
    private long maxPosterSize;

    @Value("${media.poster.base-url}")
    private String posterBaseUrl;

    @Value("${media.poster.dir}")
    private String posterDirectory;

    @Value("${media.poster.cache-control}")
    private String cacheControlHeader;

    @Value("${media.poster.default-content-type}")
    private String defaultContentType;

    private final UserContextLogger userContextLogger;

    @PostConstruct
    private void initializePosterDirectory() {
        try {
            Path posterPath = Paths.get(posterDirectory);
            if (!Files.exists(posterPath)) {
                Files.createDirectories(posterPath);
                userContextLogger.logUserOperation("INIT", "Created poster directory: " + posterDirectory);
            }
        } catch (IOException e) {
            userContextLogger.logUserOperation("INIT", "Failed to create poster directory: " + e.getMessage());
            throw new RuntimeException("Failed to initialize poster directory", e);
        }
    }

    @Cacheable(value = "posterCache", key = "#filename")
    public ResponseEntity<Resource> handleGetPosterRequest(String filename) {
        try {
            userContextLogger.logUserOperation("GET_POSTER", "Retrieving: " + filename);
            Resource posterResource = getPosterResource(filename);
            HttpHeaders headers = createResponseHeaders(posterResource, filename);
            return ResponseEntity.status(HttpStatus.OK)
                    .headers(headers)
                    .body(posterResource);
        } catch (IOException e) {
            userContextLogger.logUserOperation("GET_POSTER", "Not found: " + filename);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            userContextLogger.logUserOperation("GET_POSTER", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public String savePosterImage(String base64Image) throws ValidationException {
        try {
            validateBase64Image(base64Image);
            String[] imageParts = parseBase64Image(base64Image);
            String filename = generateFilenameFromImage(imageParts[0]);
            byte[] imageBytes = decodeAndValidateImage(imageParts[1]);
            saveImageToFile(imageBytes, filename);
            userContextLogger.logUserOperation("SAVE_POSTER", "Saved poster: " + filename);
            return filename;
        } catch (IOException e) {
            userContextLogger.logUserOperation("SAVE_POSTER", "Error: " + e.getMessage());
            throw new ValidationException("Failed to save poster image");
        }
    }

    private void validateBase64Image(String base64Image) throws ValidationException {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            throw new ValidationException("Poster image cannot be null or empty");
        }
        if (!base64Image.startsWith("data:image/")) {
            throw new ValidationException("Invalid image format - must be base64 encoded image");
        }
    }

    private String extractMimeType(String dataPrefix) throws ValidationException {
        if (!dataPrefix.contains("data:") || !dataPrefix.contains(";base64")) {
            throw new ValidationException("Invalid data URL format");
        }

        return dataPrefix.substring(5, dataPrefix.indexOf(";"));
    }

    private String getExtensionFromMimeType(String mimeType) throws ValidationException {
        return ImageType.fromMimeType(mimeType)
                .map(ImageType::getExtension)
                .orElseThrow(() -> new ValidationException("Unsupported image type: " + mimeType));
    }

    private String generateUniqueFilename(String extension) {
        return UUID.randomUUID().toString() + "." + extension;
    }

    private void validateImageSize(byte[] imageBytes) throws ValidationException {
        if (imageBytes.length > maxPosterSize) {
            long maxSizeMB = maxPosterSize / (1024 * 1024);
            throw new ValidationException("Image size cannot exceed " + maxSizeMB + "MB");
        }
    }

    private Path createPosterDirectory() throws IOException {
        Path posterPath = Paths.get(posterDirectory);
        if (!Files.exists(posterPath)) {
            Files.createDirectories(posterPath);
        }
        return posterPath;
    }

    private Resource getPosterResource(String filename) throws IOException {
        Path filePath = Paths.get(posterDirectory).resolve(filename).normalize();
        if (!filePath.startsWith(Paths.get(posterDirectory))) {
            throw new SecurityException("Path traversal attempt detected");
        }
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists() && resource.isReadable()) {
            return resource;
        }
        throw new NoSuchFileException("Poster not found: " + filename);
    }

    private HttpHeaders createResponseHeaders(Resource resource, String filename)
            throws IOException {
        HttpHeaders headers = new HttpHeaders();
        String contentType = getContentType(filename);
        long contentLength = resource.contentLength();
        long lastModified = resource.lastModified();
        String etag = "\"" + filename + "_" + lastModified + "\"";
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(contentLength);
        headers.setETag(etag);
        headers.setCacheControl(cacheControlHeader);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");
        return headers;
    }

    private String getContentType(String filename) {
        return MediaTypeFactory.getMediaType(filename)
                .map(MediaType::toString)
                .orElse(defaultContentType);
    }

    private String[] parseBase64Image(String base64Image) throws ValidationException {
        String[] parts = base64Image.split(",");
        if (parts.length != 2) {
            throw new ValidationException("Invalid base64 image format");
        }
        return parts;
    }

    private String generateFilenameFromImage(String dataPrefix) throws ValidationException {
        String mimeType = extractMimeType(dataPrefix);
        String extension = getExtensionFromMimeType(mimeType);
        return generateUniqueFilename(extension);
    }

    private byte[] decodeAndValidateImage(String base64Data) throws ValidationException {
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);
        validateImageSize(imageBytes);
        return imageBytes;
    }

    private void saveImageToFile(byte[] imageBytes, String filename) throws IOException {
        Path posterPath = createPosterDirectory();
        Path filePath = posterPath.resolve(filename);
        Files.write(filePath, imageBytes);
    }

    public String generatePosterUrl(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return null;
        }
        return posterBaseUrl + "/" + filename;
    }

}
