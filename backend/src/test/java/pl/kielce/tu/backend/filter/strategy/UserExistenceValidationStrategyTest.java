package pl.kielce.tu.backend.filter.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletResponse;
import pl.kielce.tu.backend.filter.util.ResponseHelper;
import pl.kielce.tu.backend.model.constant.TokenValidationNames;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.UserRepository;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class UserExistenceValidationStrategyTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResponseHelper responseHelper;

    @Mock
    private UserContextLogger userContextLogger;

    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private UserExistenceValidationStrategy strategy;

    @Test
    void validate_userExists_returnsValidResultContainingUser() throws Exception {
        Long userId = 42L;
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ValidationResult result = strategy.validate(userId, httpServletResponse, "/some/path");

        verify(responseHelper, never()).sendUnauthorized(any(), anyString());
        assertNotNull(result, "ValidationResult should not be null");
        assertTrue(result.isSuccess(), "Expected ValidationResult to be successful");
        assertSame(user, result.getData(), "ValidationResult should contain the same User instance");
    }

    @Test
    void validate_userNotFound_sendsUnauthorizedAndReturnsInvalidResult() throws Exception {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ValidationResult result = strategy.validate(userId, httpServletResponse, "/other/path");

        verify(responseHelper, times(1)).sendUnauthorized(httpServletResponse, "User not found");

        assertNotNull(result, "ValidationResult should not be null");
        assertFalse(result.isSuccess(), "Expected ValidationResult to be unsuccessful");
        assertNull(result.getData(), "ValidationResult should not contain a User when not found");
    }

    @Test
    void getName_returnsUserExistence() {
        assertEquals(TokenValidationNames.USER_EXISTENCE, strategy.getName());
    }
}
