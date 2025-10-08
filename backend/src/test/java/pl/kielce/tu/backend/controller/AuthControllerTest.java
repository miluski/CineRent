package pl.kielce.tu.backend.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.kielce.tu.backend.model.dto.UserDto;
import pl.kielce.tu.backend.service.auth.AuthService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void register_delegatesToService_andReturnsResponse() {
        UserDto userDto = mock(UserDto.class);
        ResponseEntity<Void> expected = ResponseEntity.status(201).build();

        when(authService.handleRegister(userDto)).thenReturn(expected);

        ResponseEntity<Void> actual = authController.register(userDto);

        assertSame(expected, actual);
        verify(authService, times(1)).handleRegister(userDto);
        verifyNoMoreInteractions(authService);
    }

    @Test
    void login_delegatesToService_andReturnsResponse() {
        UserDto userDto = mock(UserDto.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ResponseEntity<Void> expected = ResponseEntity.ok().build();

        when(authService.handleLogin(userDto, response)).thenReturn(expected);

        ResponseEntity<Void> actual = authController.login(userDto, response);

        assertSame(expected, actual);
        verify(authService, times(1)).handleLogin(userDto, response);
        verifyNoMoreInteractions(authService);
    }

    @Test
    void logout_delegatesToService_andReturnsResponse() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ResponseEntity<Void> expected = ResponseEntity.ok().build();

        when(authService.handleLogout(request, response)).thenReturn(expected);

        ResponseEntity<Void> actual = authController.logout(request, response);

        assertSame(expected, actual);
        verify(authService, times(1)).handleLogout(request, response);
        verifyNoMoreInteractions(authService);
    }

    @Test
    void refreshTokens_delegatesToService_andReturnsResponse() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ResponseEntity<Void> expected = ResponseEntity.ok().build();

        when(authService.handleRefreshTokens(request, response)).thenReturn(expected);

        ResponseEntity<Void> actual = authController.refreshTokens(request, response);

        assertSame(expected, actual);
        verify(authService, times(1)).handleRefreshTokens(request, response);
        verifyNoMoreInteractions(authService);
    }
}
