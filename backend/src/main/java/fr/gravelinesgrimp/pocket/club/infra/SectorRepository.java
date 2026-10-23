package fr.gravelinesgrimp.pocket.club.infra;

import fr.gravelinesgrimp.pocket.club.domain.Sector;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface SectorRepository extends JpaRepository<Sector, UUID> {

    /** Index {@code sector_name_key}: names are unique whatever the case. */
    @Query(
            "select case when count(s) > 0 then true else false end from Sector s"
                    + " where lower(s.name) = lower(:name) and s.id <> :excludedId")
    boolean nameTakenByAnother(String name, UUID excludedId);
}
