package pl.kielce.tu.backend.model.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dvds")
public class Dvd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @ManyToMany
    @JoinTable(name = "dvd_genres", joinColumns = @JoinColumn(name = "dvd_id"), inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private List<Genre> genres;

    @Column(name = "release_year", nullable = false)
    private Integer releaseYear;

    @Column(name = "directors", nullable = false)
    private List<String> directors;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "poster_url", nullable = false)
    private String posterUrl;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @Builder.Default
    @Column(name = "avalaible", nullable = false)
    private Boolean avalaible = false;

    @Builder.Default
    @Column(name = "copies_avalaible", nullable = false)
    private Integer copiesAvalaible = 0;

    @Builder.Default
    @Column(name = "rental_price_per_day", nullable = false)
    private Float rentalPricePerDay = 0.00f;

    @PrePersist
    protected void onCreate() {
        if (addedAt == null) {
            addedAt = LocalDateTime.now();
        }
    }

}
