package pl.kielce.tu.backend.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

class GenreTest {

    @Test
    void builderCreatesObjectWithGivenValues() {
        Genre genre = Genre.builder()
                .id(1L)
                .name("Rock")
                .build();

        assertEquals(1L, genre.getId());
        assertEquals("Rock", genre.getName());
    }

    @Test
    void noArgsConstructorAndSettersWork() {
        Genre genre = new Genre();
        genre.setId(2L);
        genre.setName("Jazz");

        assertEquals(2L, genre.getId());
        assertEquals("Jazz", genre.getName());
    }

    @Test
    void equalsAndHashCodeBasedOnFields() {
        Genre g1 = Genre.builder().id(1L).name("Pop").build();
        Genre g2 = Genre.builder().id(1L).name("Pop").build();
        Genre g3 = Genre.builder().id(2L).name("Pop").build();

        assertEquals(g1, g2);
        assertEquals(g1.hashCode(), g2.hashCode());

        assertNotEquals(g1, g3);
    }

    @Test
    void toStringContainsClassNameAndFields() {
        Genre genre = Genre.builder().id(5L).name("Classical").build();
        String s = genre.toString();

        assertTrue(s.contains("Genre"));
        assertTrue(s.contains("5"));
        assertTrue(s.contains("Classical"));
    }

    @Test
    void jpaAnnotationsArePresentAndConfigured() throws NoSuchFieldException {
        assertTrue(Genre.class.isAnnotationPresent(Entity.class), "Missing @Entity");
        Table table = Genre.class.getAnnotation(Table.class);
        assertNotNull(table, "Missing @Table");
        assertEquals("genres", table.name(), "Unexpected table name");

        Field idField = Genre.class.getDeclaredField("id");
        assertTrue(idField.isAnnotationPresent(Id.class), "Missing @Id on id field");
        assertTrue(idField.isAnnotationPresent(GeneratedValue.class), "Missing @GeneratedValue on id field");
        GeneratedValue gv = idField.getAnnotation(GeneratedValue.class);
        assertEquals(GenerationType.IDENTITY, gv.strategy(), "Expected GenerationType.IDENTITY");

        Field nameField = Genre.class.getDeclaredField("name");
        Column column = nameField.getAnnotation(Column.class);
        assertNotNull(column, "Missing @Column on name field");
        assertEquals("name", column.name(), "Unexpected column name for name field");
        assertFalse(column.nullable(), "name column should be nullable = false");
        assertTrue(column.unique(), "name column should be unique = true");
    }
}
