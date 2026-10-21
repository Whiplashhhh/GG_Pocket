package fr.gravelinesgrimp.pocket.club.infra;

import fr.gravelinesgrimp.pocket.club.domain.HoldColor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface HoldColorRepository extends JpaRepository<HoldColor, UUID> {

    /** Index {@code hold_color_label_key}: labels are unique whatever the case. */
    @Query(
            "select case when count(h) > 0 then true else false end from HoldColor h"
                    + " where lower(h.label) = lower(:label) and h.id <> :excludedId")
    boolean labelTakenByAnother(String label, UUID excludedId);
}
