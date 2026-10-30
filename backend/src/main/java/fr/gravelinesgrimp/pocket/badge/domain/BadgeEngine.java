package fr.gravelinesgrimp.pocket.badge.domain;

import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Every badge of a climber from their whole history (spec §9.1, §9.5). Pure and deterministic: the
 * sessions are replayed oldest first (day, then save time), and each award goes to the session that
 * met its condition. An award only depends on the sessions before it, so changing a session never
 * moves the awards of earlier ones (frozen months stay as they were).
 *
 * <p>Climbing days, not sessions, count for the weeks, the months and the "séances réalisées" tiers
 * (D-004): those awards go to the first session of the day.
 */
public final class BadgeEngine {

    static final int WEEK_STREAK_MIN_DAYS = 2;

    private BadgeEngine() {}

    public static List<Award> awards(List<ClimbRecord> history) {
        var replay = new Replay();
        history.stream()
                .sorted(
                        Comparator.comparing(ClimbRecord::climbedOn)
                                .thenComparing(ClimbRecord::savedAt)
                                .thenComparing(ClimbRecord::sessionId))
                .forEach(replay::session);
        return List.copyOf(replay.awards);
    }

    /** The replay of one history, session by session. */
    private static final class Replay {

        private final List<Award> awards = new ArrayList<>();
        private final Set<BadgeCode> tiersEarned = new HashSet<>();
        private final Map<LocalDate, Integer> daysPerWeek = new HashMap<>();
        private final Map<YearMonth, Integer> daysPerMonth = new HashMap<>();
        private final Set<UUID> flashedRoutes = new HashSet<>();
        private LocalDate lastDay;
        private int climbingDays;
        private int topped;
        private int lead;
        private int sessions;
        private int recordTopped;
        private LocalDate lastStreakWeek;
        private int streakWeeks;

        void session(ClimbRecord session) {
            sessions++;
            if (!session.climbedOn().equals(lastDay)) {
                climbingDay(session);
            }
            for (var line : session.ascents()) {
                ascent(session, line);
            }
            sessionWide(session);
        }

        private void climbingDay(ClimbRecord session) {
            var day = session.climbedOn();
            if (lastDay != null
                    && ChronoUnit.DAYS.between(lastDay, day) >= BadgeCode.COMEBACK.threshold()) {
                award(BadgeCode.COMEBACK, day.toString(), session);
            }
            lastDay = day;
            climbingDays++;
            tiers(BadgeFamily.CLIMBING_DAYS, climbingDays, session);

            var week = mondayOf(day);
            int inWeek = daysPerWeek.merge(week, 1, Integer::sum);
            for (var code : List.of(BadgeCode.WEEK_2, BadgeCode.WEEK_3, BadgeCode.WEEK_4)) {
                if (inWeek == code.threshold()) {
                    award(code, week.toString(), session);
                }
            }
            if (inWeek == WEEK_STREAK_MIN_DAYS) {
                streakWeek(week, session);
            }
            if (inWeek == 1) {
                monthWithoutMiss(week, session);
            }

            var month = YearMonth.from(day);
            int inMonth = daysPerMonth.merge(month, 1, Integer::sum);
            for (var code : List.of(BadgeCode.MONTH_COMPLETE, BadgeCode.MONTH_12)) {
                if (inMonth == code.threshold()) {
                    award(code, month.toString(), session);
                }
            }
        }

        /** A week just reached its days: blocks of consecutive weeks, without overlap. */
        private void streakWeek(LocalDate week, ClimbRecord session) {
            streakWeeks = week.minusWeeks(1).equals(lastStreakWeek) ? streakWeeks + 1 : 1;
            lastStreakWeek = week;
            if (streakWeeks == BadgeCode.STREAK_3W.threshold()) {
                var firstWeek = week.minusWeeks(BadgeCode.STREAK_3W.threshold() - 1L);
                award(BadgeCode.STREAK_3W, firstWeek.toString(), session);
                streakWeeks = 0;
            }
        }

        /**
         * A week just got its first day. The weeks belong to the month of their Monday, and come in
         * order: the month is done when its last week is.
         */
        private void monthWithoutMiss(LocalDate week, ClimbRecord session) {
            var month = YearMonth.from(week);
            if (!month.equals(YearMonth.from(week.plusWeeks(1)))) {
                var monday = month.atDay(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
                var everyWeek = true;
                for (; !monday.isAfter(week); monday = monday.plusWeeks(1)) {
                    everyWeek &= daysPerWeek.containsKey(monday);
                }
                if (everyWeek) {
                    award(BadgeCode.MONTH_NO_MISS, month.toString(), session);
                }
            }
        }

        private void ascent(ClimbRecord session, ClimbRecord.Line line) {
            if (line.topped()) {
                topped++;
                tiers(BadgeFamily.TOPPED, topped, session);
                if (line.mode() == ClimbMode.LEAD) {
                    lead++;
                    tiers(BadgeFamily.LEAD, lead, session);
                }
                if (line.grade() != null) {
                    tiers(BadgeFamily.FIRST_GRADE, GradeScale.rank(line.grade()), session);
                }
            }
            if (isFlash(session.climbedOn(), line) && flashedRoutes.add(line.routeId())) {
                award(BadgeCode.FLASH, line.routeId().toString(), session);
            }
        }

        private void sessionWide(ClimbRecord session) {
            var ascents = session.ascents();
            var key = session.sessionId().toString();
            if (ascents.size() >= BadgeCode.FULL_LEAD.threshold()
                    && ascents.stream().allMatch(line -> line.mode() == ClimbMode.LEAD)) {
                award(BadgeCode.FULL_LEAD, key, session);
            }
            if (ascents.size() >= BadgeCode.PERFECT.threshold()
                    && ascents.stream().allMatch(ClimbRecord.Line::clean)) {
                award(BadgeCode.PERFECT, key, session);
            }
            var toppedHere = (int) ascents.stream().filter(ClimbRecord.Line::topped).count();
            if (toppedHere >= BadgeCode.ZERO_REST.threshold()) {
                award(BadgeCode.ZERO_REST, key, session);
            }
            if (sessions >= BadgeCode.RECORD.threshold() && toppedHere > recordTopped) {
                award(BadgeCode.RECORD, key, session);
            }
            recordTopped = Math.max(recordTopped, toppedHere);
        }

        /** Every tier of the family up to {@code value}, once each. */
        private void tiers(BadgeFamily family, int value, ClimbRecord session) {
            for (var code : BadgeCode.tiers(family)) {
                if (value >= code.threshold() && tiersEarned.add(code)) {
                    award(code, "", session);
                }
            }
        }

        private void award(BadgeCode code, String key, ClimbRecord session) {
            awards.add(new Award(code, key, session.sessionId(), session.climbedOn()));
        }
    }

    static boolean isFlash(LocalDate climbedOn, ClimbRecord.Line line) {
        if (!line.clean() || !line.firstTry() || line.routeOpenedOn() == null) {
            return false;
        }
        var age = ChronoUnit.DAYS.between(line.routeOpenedOn(), climbedOn);
        return age >= 0 && age <= BadgeCode.FLASH.threshold();
    }

    static LocalDate mondayOf(LocalDate day) {
        return day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
