package pl.kielce.tu.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pl.kielce.tu.backend.model.entity.Dvd;

@Repository
public interface DvdRepository extends JpaRepository<Dvd, Long> {
}
