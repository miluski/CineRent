package pl.kielce.tu.backend.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class DvdTest {

    @Test
    void builderShouldProvideDefaultsAndPopulateFields() {
        List<Genre> emptyGenres = Collections.emptyList();
        List<String> emptyDirectors = Collections.emptyList();

        Dvd dvd = Dvd.builder()
                .title("Example Title")
                .genres(emptyGenres)
                .releaseYear(2021)
                .directors(emptyDirectors)
                .description("Some description")
                .durationMinutes(120)
                .posterUrl("http://example.com/poster.jpg")
                .build();

        assertNull(dvd.getId(), "id should be null for a built entity without persistence");
        assertEquals("Example Title", dvd.getTitle());
        assertSame(emptyGenres, dvd.getGenres());
        assertEquals(2021, dvd.getReleaseYear());
        assertSame(emptyDirectors, dvd.getDirectors());
        assertEquals("Some description", dvd.getDescription());
        assertEquals(120, dvd.getDurationMinutes());
        assertEquals("http://example.com/poster.jpg", dvd.getPosterUrl());
        assertNull(dvd.getAddedAt(), "addedAt should be null until @PrePersist is called");

        assertFalse(dvd.getAvalaible());
        assertEquals(0, dvd.getCopiesAvalaible());
        assertEquals(0.00f, dvd.getRentalPricePerDay(), 0.0f);
    }

    @Test
    void settersAndGettersShouldWork() {
        Dvd dvd = new Dvd();

        LocalDateTime added = LocalDateTime.now();
        dvd.setId(42L);
        dvd.setTitle("A Title");
        dvd.setGenres(Collections.emptyList());
        dvd.setReleaseYear(1999);
        dvd.setDirectors(Collections.singletonList("Director Name"));
        dvd.setDescription("Desc");
        dvd.setDurationMinutes(90);
        dvd.setPosterUrl("poster");
        dvd.setAddedAt(added);
        dvd.setAvalaible(true);
        dvd.setCopiesAvalaible(3);
        dvd.setRentalPricePerDay(4.5f);

        assertEquals(42L, dvd.getId());
        assertEquals("A Title", dvd.getTitle());
        assertEquals(1999, dvd.getReleaseYear());
        assertEquals(Collections.singletonList("Director Name"), dvd.getDirectors());
        assertEquals("Desc", dvd.getDescription());
        assertEquals(90, dvd.getDurationMinutes());
        assertEquals("poster", dvd.getPosterUrl());
        assertSame(added, dvd.getAddedAt());
        assertTrue(dvd.getAvalaible());
        assertEquals(3, dvd.getCopiesAvalaible());
        assertEquals(4.5f, dvd.getRentalPricePerDay(), 0.0f);
    }

    @Test
    void equalsAndHashCodeShouldConsiderFields() {
        List<String> directors = Collections.singletonList("D");
        List<Genre> genres = Collections.emptyList();

        Dvd a = Dvd.builder()
                .title("Same")
                .genres(genres)
                .releaseYear(2000)
                .directors(directors)
                .description("d")
                .durationMinutes(100)
                .posterUrl("p")
                .build();

        Dvd b = Dvd.builder()
                .title("Same")
                .genres(genres)
                .releaseYear(2000)
                .directors(directors)
                .description("d")
                .durationMinutes(100)
                .posterUrl("p")
                .build();

        assertEquals(a, b, "Objects with the same field values should be equal");
        assertEquals(a.hashCode(), b.hashCode(), "Equal objects should have same hashCode");
    }

    @Test
    void prePersistShouldSetAddedAtWhenNull() {
        Dvd dvd = new Dvd();

        dvd.onCreate();

        assertNotNull(dvd.getAddedAt(), "addedAt should be set automatically");
    }

    @Test
    void prePersistShouldNotOverwriteExistingAddedAt() {
        Dvd dvd = new Dvd();
        LocalDateTime existingTime = LocalDateTime.of(2020, 1, 1, 12, 0);
        dvd.setAddedAt(existingTime);

        dvd.onCreate();

        assertEquals(existingTime, dvd.getAddedAt(), "Existing addedAt should not be overwritten");
    }

    @Test
    void prePersistShouldSetAddedAtToCurrentTime() {
        Dvd dvd = new Dvd();
        LocalDateTime beforeCall = LocalDateTime.now();

        dvd.onCreate();

        LocalDateTime afterCall = LocalDateTime.now();
        assertNotNull(dvd.getAddedAt(), "addedAt should be set");
        assertTrue(dvd.getAddedAt().isAfter(beforeCall.minusSeconds(1)) &&
                dvd.getAddedAt().isBefore(afterCall.plusSeconds(1)),
                "addedAt should be set to approximately current time");
    }
}
