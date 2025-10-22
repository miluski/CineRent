package pl.kielce.tu.backend.service.resource.validation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ImageType;

@Component
@RequiredArgsConstructor
public class ResourceValidationService {

    @Value("${media.poster.max-size}")
    private long maxPosterSize;

    public void validateBase64Image(String base64Image) throws ValidationException {
        validateImageNotEmpty(base64Image);
        validateImageFormat(base64Image);
    }

    public void validateImageSize(byte[] imageBytes) throws ValidationException {
        if (imageBytes.length > maxPosterSize) {
            long maxSizeMB = maxPosterSize / (1024 * 1024);
            throw new ValidationException("Image size cannot exceed " + maxSizeMB + "MB");
        }
    }

    public String extractMimeType(String dataPrefix) throws ValidationException {
        validateDataUrlFormat(dataPrefix);
        return dataPrefix.substring(5, dataPrefix.indexOf(";"));
    }

    public String getExtensionFromMimeType(String mimeType) throws ValidationException {
        return ImageType.fromMimeType(mimeType)
                .map(ImageType::getExtension)
                .orElseThrow(() -> new ValidationException("Unsupported image type: " + mimeType));
    }

    public String[] parseBase64Image(String base64Image) throws ValidationException {
        String[] parts = base64Image.split(",");
        if (parts.length != 2) {
            throw new ValidationException("Invalid base64 image format");
        }
        return parts;
    }

    private void validateImageNotEmpty(String base64Image) throws ValidationException {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            throw new ValidationException("Poster image cannot be null or empty");
        }
    }

    private void validateImageFormat(String base64Image) throws ValidationException {
        if (!base64Image.startsWith("data:image/")) {
            throw new ValidationException("Invalid image format - must be base64 encoded image");
        }
    }

    private void validateDataUrlFormat(String dataPrefix) throws ValidationException {
        if (!dataPrefix.contains("data:") || !dataPrefix.contains(";base64")) {
            throw new ValidationException("Invalid data URL format");
        }
    }

}
