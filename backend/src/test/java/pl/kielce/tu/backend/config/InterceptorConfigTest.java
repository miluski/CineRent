package pl.kielce.tu.backend.config;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import pl.kielce.tu.backend.interceptor.EndpointLoggingInterceptor;

class InterceptorConfigTest {

    @Test
    void addInterceptors_shouldRegisterEndpointLoggingInterceptor() {
        EndpointLoggingInterceptor interceptor = Mockito.mock(EndpointLoggingInterceptor.class);
        InterceptorConfig config = new InterceptorConfig(interceptor);

        InterceptorRegistry registry = Mockito.mock(InterceptorRegistry.class);
        InterceptorRegistration registration = Mockito.mock(InterceptorRegistration.class);
        Mockito.when(registry.addInterceptor(interceptor)).thenReturn(registration);

        config.addInterceptors(registry);

        Mockito.verify(registry).addInterceptor(interceptor);
        Mockito.verifyNoMoreInteractions(registry);
    }
}
