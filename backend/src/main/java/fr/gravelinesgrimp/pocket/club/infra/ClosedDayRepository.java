package fr.gravelinesgrimp.pocket.club.infra;

import fr.gravelinesgrimp.pocket.club.domain.ClosedDay;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ClosedDayRepository extends JpaRepository<ClosedDay, UUID> {

    List<ClosedDay> findAllByOrderByClosedOnAsc();

    List<ClosedDay> findAllByClosedOnBetweenOrderByClosedOnAsc(LocalDate from, LocalDate to);
}
