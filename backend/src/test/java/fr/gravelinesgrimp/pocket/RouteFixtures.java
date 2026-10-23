package fr.gravelinesgrimp.pocket;

import fr.gravelinesgrimp.pocket.club.domain.Cord;
import fr.gravelinesgrimp.pocket.club.domain.Grade;
import fr.gravelinesgrimp.pocket.club.domain.HoldColor;
import fr.gravelinesgrimp.pocket.club.infra.CordRepository;
import fr.gravelinesgrimp.pocket.club.infra.GradeRepository;
import fr.gravelinesgrimp.pocket.club.infra.HoldColorRepository;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.topo.domain.Route;
import fr.gravelinesgrimp.pocket.topo.domain.RouteDraft;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStatus;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStyle;
import fr.gravelinesgrimp.pocket.topo.infra.RouteRepository;

import org.springframework.boot.test.context.TestComponent;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Routes created straight in the database, on the club configuration seeded by the migrations
 * (cords 1 to 29, the 14 colours, grades 4a- to 7c+).
 */
@TestComponent
public class RouteFixtures {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    private final RouteRepository routes;
    private final GradeRepository grades;
    private final CordRepository cords;
    private final HoldColorRepository holdColors;
    private final Clock clock;

    public RouteFixtures(
            RouteRepository routes,
            GradeRepository grades,
            CordRepository cords,
            HoldColorRepository holdColors,
            Clock clock) {
        this.routes = routes;
        this.grades = grades;
        this.cords = cords;
        this.holdColors = holdColors;
        this.clock = clock;
    }

    /** An active 6a on cord 1, yellow, opened yesterday by {@code setter}. */
    public Route create(Member setter) {
        return create(setter, RouteStatus.ACTIVE);
    }

    public Route create(Member setter, RouteStatus status) {
        var route = new Route(draft(setter), setter.id());
        switch (status) {
            case ACTIVE -> {}
            case LAST_CHANCE -> route.announceLastChance(null, ClubTime.today(clock));
            case DISMOUNTED -> route.dismount(clock.instant().minus(Duration.ofDays(3)));
        }
        return routes.saveAndFlush(route);
    }

    /** A valid draft with a unique name. */
    public RouteDraft draft(Member setter) {
        return new RouteDraft(
                "Voie de test " + SEQUENCE.incrementAndGet(),
                gradeId("6a"),
                cordId(1),
                List.of(holdColorId("Jaune")),
                ClubTime.today(clock).minusDays(1),
                List.of(setter.id()),
                null,
                "Pieds sur les bacs, mains sur les plats.",
                List.of(RouteStyle.SLAB, RouteStyle.CRIMPS));
    }

    public UUID gradeId(String code) {
        return grades.findAll().stream()
                .filter(grade -> grade.code().equals(code))
                .map(Grade::id)
                .findFirst()
                .orElseThrow();
    }

    public UUID cordId(int number) {
        return cords.findAll().stream()
                .filter(cord -> cord.number() == number)
                .map(Cord::id)
                .findFirst()
                .orElseThrow();
    }

    public UUID holdColorId(String label) {
        return holdColors.findAll().stream()
                .filter(color -> color.label().equals(label))
                .map(HoldColor::id)
                .findFirst()
                .orElseThrow();
    }
}
