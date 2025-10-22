package pl.kielce.tu.backend.service.resource.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import pl.kielce.tu.backend.exception.ValidationException;

class ResourceValidationServiceTest {

    private ResourceValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ResourceValidationService();
        ReflectionTestUtils.setField(validationService, "maxPosterSize", 5242880L);
    }

    @Test
    void validateBase64Image_validImage_noException() throws Exception {
        String validImage = "data:image/png;base64,iVBORw0KGgoAAAANS";

        validationService.validateBase64Image(validImage);
    }

    @Test
    void validateBase64Image_nullImage_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validationService.validateBase64Image(null));
    }

    @Test
    void validateBase64Image_emptyImage_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validationService.validateBase64Image(""));
    }

    @Test
    void validateBase64Image_whitespaceOnly_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validationService.validateBase64Image("   "));
    }

    @Test
    void validateBase64Image_invalidFormat_throwsValidationException() {
        String invalidImage = "not-a-data-url";

        assertThrows(ValidationException.class, () -> validationService.validateBase64Image(invalidImage));
    }

    @Test
    void validateBase64Image_missingDataPrefix_throwsValidationException() {
        String invalidImage = "image/png;base64,iVBORw0KGgoAAAANS";

        assertThrows(ValidationException.class, () -> validationService.validateBase64Image(invalidImage));
    }

    @Test
    void validateImageSize_withinLimit_noException() throws Exception {
        byte[] smallImage = new byte[1024 * 1024]; // 1MB

        validationService.validateImageSize(smallImage);
    }

    @Test
    void validateImageSize_exceedsLimit_throwsValidationException() {
        byte[] largeImage = new byte[6 * 1024 * 1024]; // 6MB

        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateImageSize(largeImage));
        assertEquals(true, exception.getMessage().contains("cannot exceed 5MB"));
    }

    @Test
    void validateImageSize_exactLimit_noException() throws Exception {
        byte[] exactImage = new byte[5242880]; // Exactly 5MB

        validationService.validateImageSize(exactImage);
    }

    @Test
    void extractMimeType_validPng_returnsPngMimeType() throws Exception {
        String dataPrefix = "data:image/png;base64";

        String mimeType = validationService.extractMimeType(dataPrefix);

        assertEquals("image/png", mimeType);
    }

    @Test
    void extractMimeType_validJpeg_returnsJpegMimeType() throws Exception {
        String dataPrefix = "data:image/jpeg;base64";

        String mimeType = validationService.extractMimeType(dataPrefix);

        assertEquals("image/jpeg", mimeType);
    }

    @Test
    void extractMimeType_validWebp_returnsWebpMimeType() throws Exception {
        String dataPrefix = "data:image/webp;base64";

        String mimeType = validationService.extractMimeType(dataPrefix);

        assertEquals("image/webp", mimeType);
    }

    @Test
    void extractMimeType_invalidFormat_throwsValidationException() {
        String invalidPrefix = "not-a-data-url";

        assertThrows(ValidationException.class, () -> validationService.extractMimeType(invalidPrefix));
    }

    @Test
    void getExtensionFromMimeType_png_returnsPngExtension() throws Exception {
        String extension = validationService.getExtensionFromMimeType("image/png");

        assertEquals("png", extension);
    }

    @Test
    void getExtensionFromMimeType_jpeg_returnsJpgExtension() throws Exception {
        String extension = validationService.getExtensionFromMimeType("image/jpeg");

        assertEquals("jpg", extension);
    }

    @Test
    void getExtensionFromMimeType_jpg_returnsJpgExtension() throws Exception {
        String extension = validationService.getExtensionFromMimeType("image/jpg");

        assertEquals("jpg", extension);
    }

    @Test
    void getExtensionFromMimeType_gif_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> validationService.getExtensionFromMimeType("image/gif"));
    }

    @Test
    void getExtensionFromMimeType_webp_returnsWebpExtension() throws Exception {
        String extension = validationService.getExtensionFromMimeType("image/webp");

        assertEquals("webp", extension);
    }

    @Test
    void getExtensionFromMimeType_unsupportedType_throwsValidationException() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.getExtensionFromMimeType("image/bmp"));
        assertEquals(true, exception.getMessage().contains("Unsupported image type"));
    }

    @Test
    void parseBase64Image_validFormat_returnsParts() throws Exception {
        String base64Image = "data:image/png;base64,iVBORw0KGgoAAAANS";

        String[] parts = validationService.parseBase64Image(base64Image);

        assertEquals(2, parts.length);
        assertEquals("data:image/png;base64", parts[0]);
        assertEquals("iVBORw0KGgoAAAANS", parts[1]);
    }

    @Test
    void parseBase64Image_multipleCommas_throwsValidationException() {
        String base64Image = "data:image/png;base64,part1,part2";

        assertThrows(ValidationException.class, () -> validationService.parseBase64Image(base64Image));
    }

    @Test
    void parseBase64Image_noComma_throwsValidationException() {
        String invalidImage = "data:image/png;base64";

        assertThrows(ValidationException.class, () -> validationService.parseBase64Image(invalidImage));
    }

    @Test
    void parseBase64Image_emptyDataPart_returnsParts() throws Exception {
        String invalidImage = ",iVBORw0KGgoAAAANS";

        String[] parts = validationService.parseBase64Image(invalidImage);

        assertEquals(2, parts.length);
        assertEquals("", parts[0]);
        assertEquals("iVBORw0KGgoAAAANS", parts[1]);
    }

    @Test
    void parseBase64Image_realBase64Data_parseCorrectly() throws Exception {
        byte[] imageBytes = new byte[] { 1, 2, 3, 4, 5 };
        String base64Data = Base64.getEncoder().encodeToString(imageBytes);
        String dataUrl = "data:image/jpeg;base64," + base64Data;

        String[] parts = validationService.parseBase64Image(dataUrl);

        assertEquals(2, parts.length);
        assertEquals("data:image/jpeg;base64", parts[0]);
        assertEquals(base64Data, parts[1]);
    }

    @Test
    void validateBase64Image_allSupportedFormats_noException() throws Exception {
        String[] supportedFormats = {
                "data:image/png;base64,test",
                "data:image/jpeg;base64,test",
                "data:image/jpg;base64,test",
                "data:image/webp;base64,test"
        };

        for (String format : supportedFormats) {
            validationService.validateBase64Image(format);
        }
    }

    @Test
    void validateImageSize_zeroSize_noException() throws Exception {
        byte[] emptyImage = new byte[0];

        validationService.validateImageSize(emptyImage);
    }
}
