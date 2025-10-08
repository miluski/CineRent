package pl.kielce.tu.backend.mapper;

import org.springframework.stereotype.Component;

import pl.kielce.tu.backend.model.entity.BlacklistedToken;

@Component
public class TokenMapper {

    public BlacklistedToken toBlacklistedToken(String token) {
        return BlacklistedToken
                .builder()
                .tokenValue(token)
                .build();
    }

}
