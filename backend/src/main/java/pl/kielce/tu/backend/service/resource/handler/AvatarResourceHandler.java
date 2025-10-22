package pl.kielce.tu.backend.service.resource.handler;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class AvatarResourceHandler {

    @Value("${media.avatar.upload-dir}")
    private String avatarDirectory;

    @Value("${media.poster.cache-control}")
    private String cacheControlHeader;

    @Value("${media.poster.default-content-type}")
    private String defaultContentType;

    private final UserContextLogger userContextLogger;

    @Cacheable(value = "avatarCache", key = "#filename")
    public ResponseEntity<Resource> handleGetRequest(String filename) {
        try {
            userContextLogger.logUserOperation("GET_AVATAR", "Retrieving: " + filename);
            Resource resource = getResource(filename);
            HttpHeaders headers = createHeaders(resource, filename);
            return buildSuccessResponse(resource, headers);
        } catch (IOException e) {
            userContextLogger.logUserOperation("GET_AVATAR", "Not found: " + filename);
            return buildNotFoundResponse();
        } catch (Exception e) {
            userContextLogger.logUserOperation("GET_AVATAR", "Error: " + e.getMessage());
            return buildErrorResponse();
        }
    }

    private Resource getResource(String filename) throws IOException {
        Path avatarPath = resolveFilePath(filename);
        validatePathSecurity(avatarPath);
        return loadResource(avatarPath, filename);
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
        return Paths.get(avatarDirectory).resolve(filename).normalize();
    }

    private void validatePathSecurity(Path filePath) {
        if (!filePath.startsWith(Paths.get(avatarDirectory))) {
            throw new SecurityException("Path traversal attempt detected");
        }
    }

    private Resource loadResource(Path filePath, String filename) throws IOException {
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists() && resource.isReadable()) {
            return resource;
        }
        throw new NoSuchFileException("Avatar not found: " + filename);
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

}
