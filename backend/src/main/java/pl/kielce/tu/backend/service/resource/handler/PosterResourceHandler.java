package pl.kielce.tu.backend.service.resource.handler;

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
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.service.resource.validation.ResourceValidationService;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class PosterResourceHandler {

    @Value("${media.poster.dir}")
    private String posterDirectory;

    @Value("${media.poster.base-url}")
    private String posterBaseUrl;

    @Value("${media.poster.cache-control}")
    private String cacheControlHeader;

    @Value("${media.poster.default-content-type}")
    private String defaultContentType;

    private final UserContextLogger userContextLogger;
    private final ResourceValidationService validationService;

    @PostConstruct
    public void initializeDirectory() {
        try {
            Path posterPath = Paths.get(posterDirectory);
            createDirectoryIfNotExists(posterPath);
            userContextLogger.logUserOperation("INIT", "Created poster directory: " + posterDirectory);
        } catch (IOException e) {
            userContextLogger.logUserOperation("INIT", "Failed to create poster directory: " + e.getMessage());
            throw new RuntimeException("Failed to initialize poster directory", e);
        }
    }

    @Cacheable(value = "posterCache", key = "#filename")
    public ResponseEntity<Resource> handleGetRequest(String filename) {
        try {
            userContextLogger.logUserOperation("GET_POSTER", "Retrieving: " + filename);
            Resource resource = getResource(filename);
            HttpHeaders headers = createHeaders(resource, filename);
            return buildSuccessResponse(resource, headers);
        } catch (IOException e) {
            userContextLogger.logUserOperation("GET_POSTER", "Not found: " + filename);
            return buildNotFoundResponse();
        } catch (Exception e) {
            userContextLogger.logUserOperation("GET_POSTER", "Error: " + e.getMessage());
            return buildErrorResponse();
        }
    }

    public String saveImage(String base64Image) throws ValidationException {
        try {
            return processImageSave(base64Image);
        } catch (IOException e) {
            userContextLogger.logUserOperation("SAVE_POSTER", "Error: " + e.getMessage());
            throw new ValidationException("Failed to save poster image");
        }
    }

    public String generateUrl(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return null;
        }
        return posterBaseUrl + "/" + filename;
    }

    private String processImageSave(String base64Image) throws ValidationException, IOException {
        validationService.validateBase64Image(base64Image);
        String[] imageParts = validationService.parseBase64Image(base64Image);
        String filename = generateFilenameFromImage(imageParts[0]);
        byte[] imageBytes = decodeAndValidateImage(imageParts[1]);
        saveImageToFile(imageBytes, filename);
        userContextLogger.logUserOperation("SAVE_POSTER", "Saved poster: " + filename);
        return filename;
    }

    private Resource getResource(String filename) throws IOException {
        Path filePath = resolveFilePath(filename);
        validatePathSecurity(filePath);
        return loadResource(filePath, filename);
    }

    private HttpHeaders createHeaders(Resource resource, String filename) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        setContentType(headers, filename);
        setContentLength(headers, resource);
        setETag(headers, filename, resource);
        setCacheControl(headers);
        setContentDisposition(headers, filename);
        return headers;
    }

    private ResponseEntity<Resource> buildSuccessResponse(Resource resource, HttpHeaders headers) {
        return ResponseEntity.status(HttpStatus.OK)
                .headers(headers)
                .body(resource);
    }

    private ResponseEntity<Resource> buildNotFoundResponse() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    private ResponseEntity<Resource> buildErrorResponse() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    private Path resolveFilePath(String filename) {
        return Paths.get(posterDirectory).resolve(filename).normalize();
    }

    private void validatePathSecurity(Path filePath) {
        if (!filePath.startsWith(Paths.get(posterDirectory))) {
            throw new SecurityException("Path traversal attempt detected");
        }
    }

    private Resource loadResource(Path filePath, String filename) throws IOException {
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists() && resource.isReadable()) {
            return resource;
        }
        throw new NoSuchFileException("Poster not found: " + filename);
    }

    private void setContentType(HttpHeaders headers, String filename) {
        String contentType = getContentType(filename);
        headers.setContentType(MediaType.parseMediaType(contentType));
    }

    private void setContentLength(HttpHeaders headers, Resource resource) throws IOException {
        headers.setContentLength(resource.contentLength());
    }

    private void setETag(HttpHeaders headers, String filename, Resource resource) throws IOException {
        long lastModified = resource.lastModified();
        String etag = "\"" + filename + "_" + lastModified + "\"";
        headers.setETag(etag);
    }

    private void setCacheControl(HttpHeaders headers) {
        headers.setCacheControl(cacheControlHeader);
    }

    private void setContentDisposition(HttpHeaders headers, String filename) {
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");
    }

    private String getContentType(String filename) {
        return MediaTypeFactory.getMediaType(filename)
                .map(MediaType::toString)
                .orElse(defaultContentType);
    }

    private String generateFilenameFromImage(String dataPrefix) throws ValidationException {
        String mimeType = validationService.extractMimeType(dataPrefix);
        String extension = validationService.getExtensionFromMimeType(mimeType);
        return generateUniqueFilename(extension);
    }

    private byte[] decodeAndValidateImage(String base64Data) throws ValidationException {
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);
        validationService.validateImageSize(imageBytes);
        return imageBytes;
    }

    private void saveImageToFile(byte[] imageBytes, String filename) throws IOException {
        Path posterPath = ensureDirectoryExists();
        Path filePath = posterPath.resolve(filename);
        Files.write(filePath, imageBytes);
    }

    private Path ensureDirectoryExists() throws IOException {
        Path posterPath = Paths.get(posterDirectory);
        createDirectoryIfNotExists(posterPath);
        return posterPath;
    }

    private void createDirectoryIfNotExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    private String generateUniqueFilename(String extension) {
        return UUID.randomUUID().toString() + "." + extension;
    }

}
