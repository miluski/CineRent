package pl.kielce.tu.backend.filter.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletResponse;
import pl.kielce.tu.backend.filter.util.ResponseHelper;
import pl.kielce.tu.backend.model.constant.AdminEndpoints;
import pl.kielce.tu.backend.model.constant.RankType;
import pl.kielce.tu.backend.model.entity.User;

@ExtendWith(MockitoExtension.class)
class AdminAccessValidationStrategyTest {

    @Test
    void whenPathIsAdminAndUserIsNotAdmin_thenForbiddenSentAndValidationResultIsFalse() throws Exception {
        ResponseHelper responseHelper = Mockito.mock(ResponseHelper.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        User user = Mockito.mock(User.class);
        when(user.getRank()).thenReturn(RankType.USER);

        String path = "/admin/some";
        try (MockedStatic<AdminEndpoints> adminEndpointsMock = Mockito.mockStatic(AdminEndpoints.class)) {
            adminEndpointsMock.when(() -> AdminEndpoints.isMember(path)).thenReturn(true);

            AdminAccessValidationStrategy strategy = new AdminAccessValidationStrategy(responseHelper);
            ValidationResult result = strategy.validate(user, response, path);

            verify(responseHelper).sendForbidden(response, "Insufficient permissions");

            assertFalse(result.isSuccess());
            assertNull(result.getData());
        }
    }

    @Test
    void whenPathIsAdminAndUserIsAdmin_thenAllowedAndNoForbiddenSent() throws Exception {
        ResponseHelper responseHelper = Mockito.mock(ResponseHelper.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        User user = Mockito.mock(User.class);
        when(user.getRank()).thenReturn(RankType.ADMIN);

        String path = "/admin/only";
        try (MockedStatic<AdminEndpoints> adminEndpointsMock = Mockito.mockStatic(AdminEndpoints.class)) {
            adminEndpointsMock.when(() -> AdminEndpoints.isMember(path)).thenReturn(true);

            AdminAccessValidationStrategy strategy = new AdminAccessValidationStrategy(responseHelper);
            ValidationResult result = strategy.validate(user, response, path);

            verifyNoInteractions(responseHelper);

            assertTrue(result.isSuccess());
            assertEquals(user, result.getData());
        }
    }

    @Test
    void whenPathIsNotAdmin_thenAllowedRegardlessOfRank() throws Exception {
        ResponseHelper responseHelper = Mockito.mock(ResponseHelper.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        User user = Mockito.mock(User.class);

        String path = "/public/resource";
        try (MockedStatic<AdminEndpoints> adminEndpointsMock = Mockito.mockStatic(AdminEndpoints.class)) {
            adminEndpointsMock.when(() -> AdminEndpoints.isMember(path)).thenReturn(false);

            AdminAccessValidationStrategy strategy = new AdminAccessValidationStrategy(responseHelper);
            ValidationResult result = strategy.validate(user, response, path);

            verifyNoInteractions(responseHelper);

            assertTrue(result.isSuccess());
            assertEquals(user, result.getData());
        }
    }
}
