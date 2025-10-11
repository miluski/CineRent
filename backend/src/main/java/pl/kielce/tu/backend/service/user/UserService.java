package pl.kielce.tu.backend.service.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.extractor.ClaimsExtractor;
import pl.kielce.tu.backend.mapper.UserMapper;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.model.dto.UserDto;
import pl.kielce.tu.backend.model.entity.Genre;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.UserRepository;
import pl.kielce.tu.backend.service.auth.CookieService;
import pl.kielce.tu.backend.service.validation.factory.ValidationStrategyFactory;

@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final UserMapper userMapper;
    private final CookieService cookieService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClaimsExtractor claimsExtractor;
    private final ValidationStrategyFactory validationStrategyFactory;

    public ResponseEntity<UserDto> handleGetUser(HttpServletRequest httpServletRequest) {
        try {
            Long userId = extractUserIdFromToken(httpServletRequest);
            User user = getUserById(userId);
            UserDto userDto = userMapper.toDto(user);
            return ResponseEntity.status(HttpStatus.OK).body(userDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<Void> handleEditUser(HttpServletRequest httpServletRequest, UserDto userDto) {
        try {
            Long userId = extractUserIdFromToken(httpServletRequest);
            validateAtLeastOneField(userDto);
            User user = getUserById(userId);
            validateAndApplyUpdates(user, userDto);
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Long extractUserIdFromToken(HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN);
        if (token == null) {
            throw new IllegalArgumentException("Missing authentication token");
        }
        return claimsExtractor.extractUserId(token, jwtSecret);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    private void validateAtLeastOneField(UserDto userDto) throws ValidationException {
        if (userDto.getNickname() == null && userDto.getPassword() == null
                && userDto.getAge() == null && userDto.getPreferredGenresIdentifiers() == null) {
            throw new ValidationException("At least one field must be provided for update");
        }
    }

    private void validateAndApplyUpdates(User user, UserDto userDto) throws ValidationException {
        updateNicknameIfPresent(user, userDto);
        updatePasswordIfPresent(user, userDto);
        updateAgeIfPresent(user, userDto);
        updatePreferredGenresIfPresent(user, userDto);
    }

    private void updateNicknameIfPresent(User user, UserDto userDto) throws ValidationException {
        if (userDto.getNickname() != null) {
            validationStrategyFactory.getStrategy(ValidationStrategyType.NICKNAME)
                    .validate(userDto.getNickname());
            user.setNickname(userDto.getNickname());
        }
    }

    private void updatePasswordIfPresent(User user, UserDto userDto) throws ValidationException {
        if (userDto.getPassword() != null) {
            validationStrategyFactory.getStrategy(ValidationStrategyType.PASSWORD)
                    .validate(userDto.getPassword());
            String encodedPassword = passwordEncoder.encode(userDto.getPassword());
            user.setPassword(encodedPassword);
        }
    }

    private void updateAgeIfPresent(User user, UserDto userDto) throws ValidationException {
        if (userDto.getAge() != null) {
            validationStrategyFactory.getStrategy(ValidationStrategyType.AGE)
                    .validate(userDto.getAge());
            user.setAge(userDto.getAge());
        }
    }

    private void updatePreferredGenresIfPresent(User user, UserDto userDto) throws ValidationException {
        if (userDto.getPreferredGenresIdentifiers() != null) {
            validationStrategyFactory.getStrategy(ValidationStrategyType.GENRE)
                    .validate(userDto.getPreferredGenresIdentifiers());
            List<Genre> genres = mapGenreIdsToGenres(userDto.getPreferredGenresIdentifiers());
            user.setPreferredGenres(genres);
        }
    }

    private List<Genre> mapGenreIdsToGenres(List<Long> genreIds) {
        return userMapper.toUser(UserDto.builder()
                .preferredGenresIdentifiers(genreIds)
                .build())
                .getPreferredGenres();
    }

}
