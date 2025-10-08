package pl.kielce.tu.backend.filter.strategy;

import jakarta.servlet.http.HttpServletRequest;
import pl.kielce.tu.backend.model.constant.CookieNames;

public record TokenPresenceInput(HttpServletRequest request, CookieNames tokenType) {
}
