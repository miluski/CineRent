package pl.kielce.tu.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void repositoryBeanExists() {
        assertNotNull(userRepository);
    }

    @Test
    void repositoryIsEmptyAtStart() {
        long count = userRepository.count();
        assertEquals(0L, count);
    }
}
