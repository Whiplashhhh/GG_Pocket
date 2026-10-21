package fr.gravelinesgrimp.pocket.club.application;

import fr.gravelinesgrimp.pocket.club.domain.ClubLayout;
import fr.gravelinesgrimp.pocket.club.infra.CordRepository;
import fr.gravelinesgrimp.pocket.club.infra.GradeRepository;
import fr.gravelinesgrimp.pocket.club.infra.HoldColorRepository;
import fr.gravelinesgrimp.pocket.club.infra.SectorRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads the whole club configuration for the services that need it (topo, config screen). Not
 * secured itself: its callers are.
 */
@Component
public class ClubLayoutReader {

    private final SectorRepository sectors;
    private final CordRepository cords;
    private final HoldColorRepository holdColors;
    private final GradeRepository grades;

    public ClubLayoutReader(
            SectorRepository sectors,
            CordRepository cords,
            HoldColorRepository holdColors,
            GradeRepository grades) {
        this.sectors = sectors;
        this.cords = cords;
        this.holdColors = holdColors;
        this.grades = grades;
    }

    @Transactional(readOnly = true)
    public ClubLayout read() {
        return ClubLayout.of(
                sectors.findAll(), cords.findAll(), holdColors.findAll(), grades.findAll());
    }
}
