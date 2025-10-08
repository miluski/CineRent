package pl.kielce.tu.backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import pl.kielce.tu.backend.filter.TokenRequestFilter;
import pl.kielce.tu.backend.model.constant.PublicEndpoints;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    TokenRequestFilter tokenRequestFilter;

    @Mock
    HttpSecurity httpSecurity;

    @Test
    @SuppressWarnings("null")
    void corsConfigurationSource_shouldConfigureCorsCorrectly() {
        SecurityConfig config = new SecurityConfig(tokenRequestFilter);
        var source = config.corsConfigurationSource();
        assertNotNull(source);
        var urlSource = (UrlBasedCorsConfigurationSource) source;
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        CorsConfiguration cors = urlSource.getCorsConfiguration(request);
        assertNotNull(cors);
        assertEquals(List.of("GET", "POST", "PUT", "PATCH", "DELETE"), cors.getAllowedMethods());
        assertEquals(List.of("*"), cors.getAllowedHeaders());
        assertNotNull(cors.getAllowedOriginPatterns());
        var originPatterns = cors.getAllowedOriginPatterns();
        assertTrue(originPatterns.contains("https://*"));
        assertTrue(originPatterns.contains("http://*"));
        assertTrue(originPatterns.contains("capacitor://*"));
        assertTrue(originPatterns.contains("ionic://*"));
        assertNotNull(cors.getExposedHeaders());
        var exposedHeaders = cors.getExposedHeaders();
        assertTrue(exposedHeaders.contains("Set-Cookie"));
        assertEquals(3600L, cors.getMaxAge());
        assertNotNull(cors.getAllowCredentials());
        Boolean allowCredentials = cors.getAllowCredentials();
        assertTrue(Boolean.TRUE.equals(allowCredentials));
    }

    @Test
    void passwordEncoder_shouldReturnArgon2AndMatchEncodedPasswords() {
        SecurityConfig config = new SecurityConfig(tokenRequestFilter);
        PasswordEncoder encoder = config.passwordEncoder();
        assertNotNull(encoder);
        assertTrue(encoder instanceof Argon2PasswordEncoder);
        String raw = "secret";
        String hashed = encoder.encode(raw);
        assertTrue(encoder.matches(raw, hashed));
    }

    @Test
    void validatorAndMethodValidationPostProcessor_shouldBePresent() {
        SecurityConfig config = new SecurityConfig(tokenRequestFilter);
        LocalValidatorFactoryBean validator = config.validator();
        assertNotNull(validator);
        MethodValidationPostProcessor postProcessor = config.methodValidationPostProcessor();
        assertNotNull(postProcessor);
    }

    @Test
    void securityFilterChain_shouldConfigureHttpSecurityAndReturnBuiltChain() throws Exception {
        SecurityConfig config = new SecurityConfig(tokenRequestFilter);

        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        when(httpSecurity.cors(any())).thenReturn(httpSecurity);
        when(httpSecurity.formLogin(any())).thenReturn(httpSecurity);
        when(httpSecurity.httpBasic(any())).thenReturn(httpSecurity);
        when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);
        when(httpSecurity.addFilterBefore(any(), any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.headers(any())).thenReturn(httpSecurity);
        when(httpSecurity.exceptionHandling(any())).thenReturn(httpSecurity);

        DefaultSecurityFilterChain expectedChain = mock(DefaultSecurityFilterChain.class);
        when(httpSecurity.build()).thenReturn(expectedChain);

        try (MockedStatic<PublicEndpoints> mocked = Mockito.mockStatic(PublicEndpoints.class)) {
            mocked.when(PublicEndpoints::getAllPatterns).thenReturn(new String[] { "/public/**" });

            SecurityFilterChain result = config.securityFilterChain(httpSecurity);
            assertSame(expectedChain, result);

            verify(httpSecurity).csrf(any());
            verify(httpSecurity).cors(any());
            verify(httpSecurity).formLogin(any());
            verify(httpSecurity).httpBasic(any());
            verify(httpSecurity).sessionManagement(any());
            verify(httpSecurity).addFilterBefore(eq(tokenRequestFilter),
                    eq(UsernamePasswordAuthenticationFilter.class));
            verify(httpSecurity).authorizeHttpRequests(any());
            verify(httpSecurity).headers(any());
            verify(httpSecurity).exceptionHandling(any());
            verify(httpSecurity).build();
        }
    }
}
