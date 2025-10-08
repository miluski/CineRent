package pl.kielce.tu.backend.filter.strategy;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;
import pl.kielce.tu.backend.model.constant.TokenValidationNames;

public interface ValidationStrategy<T> {

    ValidationResult validate(T input, HttpServletResponse response, String requestPath) throws IOException;

    TokenValidationNames getName();

}
