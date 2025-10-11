package pl.kielce.tu.backend.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class DvdRepositoryTest {

    @Autowired
    private DvdRepository dvdRepository;

    @Test
    void repositoryIsInjected() {
        assertNotNull(dvdRepository, "DvdRepository should be injected by Spring");
    }

    @Test
    void countReturnsNonNegative() {
        long count = dvdRepository.count();
        assertTrue(count >= 0, "Repository count should be non-negative");
    }
}
