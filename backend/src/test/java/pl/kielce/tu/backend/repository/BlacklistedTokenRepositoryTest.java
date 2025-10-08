package pl.kielce.tu.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import pl.kielce.tu.backend.model.entity.BlacklistedToken;

@DataJpaTest
class BlacklistedTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BlacklistedTokenRepository repository;

    @Test
    void findByToken_returnsEntity_whenExists() throws Exception {
        Class<?> clazz = BlacklistedToken.class;
        Object entity = clazz.getDeclaredConstructor().newInstance();

        Field tokenField = clazz.getDeclaredField("tokenValue");
        tokenField.setAccessible(true);
        tokenField.set(entity, "sample-token-123");

        entityManager.persistAndFlush(entity);

        Optional<BlacklistedToken> found = repository.findByToken("sample-token-123");
        assertTrue(found.isPresent());
        
        Field returnedTokenField = found.get().getClass().getDeclaredField("tokenValue");
        returnedTokenField.setAccessible(true);
        Object returnedValue = returnedTokenField.get(found.get());
        assertEquals("sample-token-123", returnedValue);
    }

    @Test
    void findByToken_returnsEmpty_whenNotExists() {
        Optional<BlacklistedToken> found = repository.findByToken("non-existent-token");
        assertFalse(found.isPresent());
    }
}
