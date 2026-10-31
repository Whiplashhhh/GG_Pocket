package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.session.domain.MonthTotals;
import fr.gravelinesgrimp.pocket.session.domain.PlanningMonth;
import fr.gravelinesgrimp.pocket.session.domain.SessionCalendar;
import fr.gravelinesgrimp.pocket.session.domain.SessionTotals;
import fr.gravelinesgrimp.pocket.session.domain.Streak;
import fr.gravelinesgrimp.pocket.session.infra.ClimbingSessionRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

/** The planning (spec §8.6): my own sessions, a month at a time. */
@Service
public class PlanningService {

    private final ClimbingSessionRepository sessions;
    private final SessionViews views;
    private final Clock clock;

    public PlanningService(ClimbingSessionRepository sessions, SessionViews views, Clock clock) {
        this.sessions = sessions;
        this.views = views;
        this.clock = clock;
    }

    /**
     * @param month {@code YYYY-MM}, or null for this month
     * @throws fr.gravelinesgrimp.pocket.platform.domain.BusinessException {@code VALIDATION_FAILED}
     *     for a malformed or future month
     */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional(readOnly = true)
    public PlanningView of(UUID memberId, String month) {
        var today = ClubTime.today(clock);
        var shown = PlanningMonth.parse(month, today);
        var inMonth =
                views.of(
                        sessions.findAllWithAscentsBetween(
                                memberId, shown.atDay(1), shown.atEndOfMonth()),
                        memberId);
        var days =
                inMonth.stream()
                        .collect(
                                Collectors.groupingBy(
                                        SessionView::climbedOn, TreeMap::new, Collectors.toList()))
                        .entrySet()
                        .stream()
                        .map(day -> new PlanningView.Day(day.getKey(), day.getValue()))
                        .toList();
        return new PlanningView(
                shown,
                SessionCalendar.isFrozen(shown.atDay(1), clock.instant()),
                days,
                MonthTotals.of(inMonth.stream().map(PlanningService::forTotals).toList()),
                Streak.of(sessions.findClimbingDays(memberId), today));
    }

    private static MonthTotals.Session forTotals(SessionView session) {
        List<SessionTotals.Line> lines =
                session.ascents().stream()
                        .map(
                                ascent ->
                                        new SessionTotals.Line(
                                                ascent.mode(),
                                                ascent.topped(),
                                                ascent.route().grade(),
                                                ascent.route().gradeRank()))
                        .toList();
        return new MonthTotals.Session(session.climbedOn(), session.feeling(), lines);
    }
}
