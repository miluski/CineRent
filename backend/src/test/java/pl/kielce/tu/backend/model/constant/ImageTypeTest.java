package pl.kielce.tu.backend.model.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class ImageTypeTest {

    @Test
    void fromMimeType_knownMimeTypes_returnExpectedEnum() {
        assertEquals(Optional.of(ImageType.JPEG), ImageType.fromMimeType("image/jpeg"));
        assertEquals(Optional.of(ImageType.JPG), ImageType.fromMimeType("image/jpg"));
        assertEquals(Optional.of(ImageType.PNG), ImageType.fromMimeType("image/png"));
        assertEquals(Optional.of(ImageType.WEBP), ImageType.fromMimeType("image/webp"));
    }

    @Test
    void fromMimeType_unknownOrNullMimeType_returnsEmptyOptional() {
        assertTrue(ImageType.fromMimeType("application/json").isEmpty());
        assertTrue(ImageType.fromMimeType("text/plain").isEmpty());
        assertTrue(ImageType.fromMimeType(null).isEmpty());
    }

    @Test
    void getters_returnConfiguredValues() {
        assertEquals("image/jpeg", ImageType.JPEG.getMimeType());
        assertEquals("jpg", ImageType.JPEG.getExtension());

        assertEquals("image/jpg", ImageType.JPG.getMimeType());
        assertEquals("jpg", ImageType.JPG.getExtension());

        assertEquals("image/png", ImageType.PNG.getMimeType());
        assertEquals("png", ImageType.PNG.getExtension());

        assertEquals("image/webp", ImageType.WEBP.getMimeType());
        assertEquals("webp", ImageType.WEBP.getExtension());
    }
}
