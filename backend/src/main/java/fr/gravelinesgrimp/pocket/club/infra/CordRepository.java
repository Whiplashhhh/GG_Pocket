package fr.gravelinesgrimp.pocket.club.infra;

import fr.gravelinesgrimp.pocket.club.domain.Cord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CordRepository extends JpaRepository<Cord, UUID> {

    List<Cord> findAllBySectorId(UUID sectorId);

    @Query(
            "select case when count(c) > 0 then true else false end from Cord c"
                    + " where c.number = :number and c.id <> :excludedId")
    boolean numberTakenByAnother(int number, UUID excludedId);
}
