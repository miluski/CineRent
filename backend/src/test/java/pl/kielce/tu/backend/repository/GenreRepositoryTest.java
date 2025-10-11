package pl.kielce.tu.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import pl.kielce.tu.backend.model.entity.Genre;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
class GenreRepositoryTest {

    @Autowired
    private GenreRepository repository;

    @Test
    void whenFindByName_existingGenre_returnsGenre() throws Exception {
        Genre genre = Genre.class.getDeclaredConstructor().newInstance();
        Field nameField = Genre.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(genre, "Fantasy");

        repository.saveAndFlush(genre);

        Optional<Genre> found = repository.findByName("Fantasy");
        assertTrue(found.isPresent(), "Expected genre to be found");
        Field foundNameField = Genre.class.getDeclaredField("name");
        foundNameField.setAccessible(true);
        Object actualName = foundNameField.get(found.get());
        assertEquals("Fantasy", actualName);
    }

    @Test
    void whenFindByName_nonExistingGenre_returnsEmpty() {
        Optional<Genre> found = repository.findByName("NonExistingGenreName");
        assertFalse(found.isPresent(), "Expected no genre to be found");
    }
}
