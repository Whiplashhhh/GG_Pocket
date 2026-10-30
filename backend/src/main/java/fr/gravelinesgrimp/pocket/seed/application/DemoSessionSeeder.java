package fr.gravelinesgrimp.pocket.seed.application;

import fr.gravelinesgrimp.pocket.club.domain.Grade;
import fr.gravelinesgrimp.pocket.club.infra.GradeRepository;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.session.application.AutoRouteChecks;
import fr.gravelinesgrimp.pocket.session.domain.Ascent;
import fr.gravelinesgrimp.pocket.session.domain.AscentDraft;
import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;
import fr.gravelinesgrimp.pocket.session.domain.RouteMode;
import fr.gravelinesgrimp.pocket.session.domain.SessionDraft;
import fr.gravelinesgrimp.pocket.session.domain.SessionVisibility;
import fr.gravelinesgrimp.pocket.session.infra.ClimbingSessionRepository;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;
import fr.gravelinesgrimp.pocket.topo.domain.Route;
import fr.gravelinesgrimp.pocket.topo.infra.RouteRepository;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Demo sessions over the last ten weeks, so that the session cards, the route statistics and (next)
 * the planning have something to show. Always the same ones for the same day (fixed seed). Dates
 * further back than a week are fine here: the seed writes history, it does not log a session.
 */
@Component
@Profile("dev")
public class DemoSessionSeeder {

    /** Days back of each session, per account; a day twice is two sessions that day. */
    private static final Map<String, List<Integer>> DAYS_BACK =
            Map.of(
                    "grimpeur",
                    List.of(
                            1, 3, 6, 8, 8, 10, 13, 15, 17, 20, 22, 27, 29, 31, 34, 36, 41, 43, 48,
                            50, 55, 57, 62, 64, 69),
                    "direction",
                    List.of(2, 5, 9, 16, 23, 30, 37, 44, 51, 58),
                    "ouvreur",
                    List.of(4, 18, 32, 46));

    /** Around the grade each account climbs. */
    private static final Map<String, String> LEVELS =
            Map.of("grimpeur", "6a", "direction", "6c", "ouvreur", "7a");

    private static final List<String> COMMENTS =
            List.of(
                    "Les avant-bras en feu, le moral au beau fixe.",
                    "Enfin passé le crux du dévers. Merci la magnésie.",
                    "Séance courte mais efficace. Demain courbatures.",
                    "J'ai découvert des muscles dont j'ignorais l'existence.",
                    "Pas mon jour. Le mur a gagné, revanche jeudi.");

    private final ClimbingSessionRepository sessions;
    private final RouteRepository routes;
    private final GradeRepository grades;
    private final AutoRouteChecks autoChecks;

    public DemoSessionSeeder(
            ClimbingSessionRepository sessions,
            RouteRepository routes,
            GradeRepository grades,
            AutoRouteChecks autoChecks) {
        this.sessions = sessions;
        this.routes = routes;
        this.grades = grades;
        this.autoChecks = autoChecks;
    }

    /**
     * Seeds the sessions once: does nothing if there are already some, or no routes.
     *
     * @return the number of sessions created
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public int seed(Map<String, Member> accounts, LocalDate today) {
        if (sessions.count() > 0) {
            return 0;
        }
        var allRoutes = routes.findAll();
        if (allRoutes.isEmpty()) {
            return 0;
        }
        var ranks =
                grades.findAll().stream().collect(Collectors.toMap(Grade::id, Function.identity()));
        var created = 0;
        var random = new Random(2026);
        // Its own sequence: the sessions stay the same as before belayers came along.
        var belaying = new Random(2027);
        var climbers = List.of("grimpeur", "direction", "ouvreur");
        for (var nickname : climbers) {
            var member = accounts.get(nickname);
            var partners =
                    climbers.stream()
                            .filter(other -> !other.equals(nickname))
                            .map(other -> accounts.get(other).id())
                            .toList();
            var level =
                    ranks.values().stream()
                            .filter(grade -> grade.code().equals(LEVELS.get(nickname)))
                            .findFirst()
                            .map(Grade::rank)
                            .orElse(20);
            var tried = new HashSet<UUID>();
            var touched = new HashSet<RouteMode>();
            // Oldest first, so that "first try" is true only the first time.
            var days =
                    DAYS_BACK.get(nickname).stream()
                            .sorted(Comparator.reverseOrder())
                            .map(today::minusDays)
                            .toList();
            for (var day : days) {
                var onTheWall = allRoutes.stream().filter(route -> onTheWall(route, day)).toList();
                if (onTheWall.isEmpty()) {
                    continue;
                }
                var ascents = new ArrayList<AscentDraft>();
                var count = 3 + random.nextInt(5);
                // Most evenings with one partner, now and then the other one; sometimes nobody
                // said.
                var partner = belaying.nextInt(partners.size());
                var named = belaying.nextInt(4) > 0;
                for (var i = 0; i < count; i++) {
                    var route = onTheWall.get(random.nextInt(onTheWall.size()));
                    var rank = ranks.get(route.gradeId()).rank();
                    var belayer =
                            named
                                    ? partners.get(
                                            belaying.nextInt(5) == 0
                                                    ? (partner + 1) % partners.size()
                                                    : partner)
                                    : null;
                    ascents.add(
                            ascent(random, route, rank <= level, tried.add(route.id()), belayer));
                }
                var draft =
                        new SessionDraft(
                                day,
                                random.nextInt(4) == 0 ? null : 5 + random.nextInt(6),
                                random.nextInt(3) == 0
                                        ? COMMENTS.get(random.nextInt(COMMENTS.size()))
                                        : null,
                                random.nextInt(6) == 0
                                        ? SessionVisibility.PRIVATE
                                        : SessionVisibility.FRIENDS,
                                ascents);
                draft.check();
                var session =
                        sessions.save(
                                ClimbingSession.create(member.id(), UUID.randomUUID(), draft));
                session.ascents().stream().map(Ascent::routeMode).forEach(touched::add);
                created++;
            }
            autoChecks.refresh(member.id(), touched);
        }
        return created;
    }

    private static boolean onTheWall(Route route, LocalDate day) {
        return !route.openedOn().isAfter(day)
                && (!route.isDismounted()
                        || !LocalDate.ofInstant(route.dismountedAt(), ClubTime.ZONE).isBefore(day));
    }

    private static AscentDraft ascent(
            Random random, Route route, boolean atLevel, boolean firstTry, UUID belayerId) {
        var mode = random.nextInt(5) < 2 ? ClimbMode.LEAD : ClimbMode.TOP_ROPE;
        var topped = random.nextInt(100) < (atLevel ? 85 : 35);
        var clean = topped && random.nextInt(100) < 55;
        var falls = clean ? 0 : topped ? random.nextInt(3) : 1 + random.nextInt(4);
        Integer feeling = random.nextBoolean() ? null : 5 + random.nextInt(6);
        return new AscentDraft(
                route.id(), mode, topped, clean, falls, firstTry, feeling, belayerId);
    }
}
