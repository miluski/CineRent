package pl.kielce.tu.backend.model.constant;

import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageType {
    JPEG("image/jpeg", "jpg"),
    JPG("image/jpg", "jpg"),
    PNG("image/png", "png"),
    WEBP("image/webp", "webp");

    private final String mimeType;
    private final String extension;

    private static final Map<String, ImageType> MIME_TYPE_MAP = Map.of(
            "image/jpeg", JPEG,
            "image/jpg", JPG,
            "image/png", PNG,
            "image/webp", WEBP);

    public static Optional<ImageType> fromMimeType(String mimeType) {
        if (mimeType == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(MIME_TYPE_MAP.get(mimeType));
    }
}
