package pl.kielce.tu.backend.service.resource;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.service.resource.handler.AvatarResourceHandler;
import pl.kielce.tu.backend.service.resource.handler.PosterResourceHandler;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final PosterResourceHandler posterHandler;
    private final AvatarResourceHandler avatarHandler;

    public ResponseEntity<Resource> handleGetPosterRequest(String filename) {
        return posterHandler.handleGetRequest(filename);
    }

    public ResponseEntity<Resource> handleGetAvatarRequest(String filename) {
        return avatarHandler.handleGetRequest(filename);
    }

    public String savePosterImage(String base64Image) throws ValidationException {
        return posterHandler.saveImage(base64Image);
    }

    public String generatePosterUrl(String filename) {
        return posterHandler.generateUrl(filename);
    }

}
