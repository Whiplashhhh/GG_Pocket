package fr.gravelinesgrimp.pocket.badge.application;

import fr.gravelinesgrimp.pocket.badge.domain.Award;
import fr.gravelinesgrimp.pocket.badge.domain.AwardDiff;
import fr.gravelinesgrimp.pocket.badge.domain.BadgeAward;
import fr.gravelinesgrimp.pocket.badge.domain.BadgeEngine;
import fr.gravelinesgrimp.pocket.badge.domain.ClimbRecord;
import fr.gravelinesgrimp.pocket.badge.infra.BadgeAwardRepository;
import fr.gravelinesgrimp.pocket.badge.infra.BadgeHistoryRepository;
import fr.gravelinesgrimp.pocket.badge.infra.HistoryRow;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Writes the badges of a climber after a change of their sessions (spec §9.3, §9.5): the engine
 * replays the whole history and the stored awards follow, a badge whose condition no longer holds
 * being taken back.
 */
@Component
public class BadgeAwards {

    private final BadgeHistoryRepository history;
    private final BadgeAwardRepository awards;

    public BadgeAwards(BadgeHistoryRepository history, BadgeAwardRepository awards) {
        this.history = history;
        this.awards = awards;
    }

    /**
     * Once the change of sessions is applied, in its transaction (the member row is locked: one
     * change of a climber at a time).
     *
     * @return the badges just unlocked, in the catalogue order
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public List<Award> refresh(UUID memberId) {
        // A deleted session took its awards with it (foreign key): read what is left.
        awards.flush();
        var computed = BadgeEngine.awards(records(history.historyOf(memberId)));
        Map<Award.Id, BadgeAward> stored =
                awards.findAllByMemberId(memberId).stream()
                        .collect(
                                Collectors.toMap(
                                        award -> award.toAward().id(), Function.identity()));
        var diff =
                AwardDiff.between(
                        stored.values().stream().map(BadgeAward::toAward).toList(), computed);
        diff.removed().forEach(id -> awards.delete(stored.get(id)));
        diff.moved().forEach(award -> stored.get(award.id()).moveTo(award));
        diff.added().forEach(award -> awards.save(new BadgeAward(memberId, award)));
        return diff.added().stream()
                .sorted(Comparator.comparing(Award::code).thenComparing(Award::earnedOn))
                .toList();
    }

    /** Rows come ordered by session, then ascent. */
    static List<ClimbRecord> records(List<HistoryRow> rows) {
        Map<UUID, List<HistoryRow>> bySession = new LinkedHashMap<>();
        rows.forEach(
                row ->
                        bySession
                                .computeIfAbsent(row.sessionId(), id -> new ArrayList<>())
                                .add(row));
        return bySession.values().stream()
                .map(
                        sessionRows -> {
                            var first = sessionRows.getFirst();
                            var lines =
                                    sessionRows.stream()
                                            .filter(row -> row.routeId() != null)
                                            .map(
                                                    row ->
                                                            new ClimbRecord.Line(
                                                                    row.routeId(),
                                                                    row.mode(),
                                                                    row.topped(),
                                                                    row.clean(),
                                                                    row.firstTry(),
                                                                    row.grade(),
                                                                    row.routeOpenedOn()))
                                            .toList();
                            return new ClimbRecord(
                                    first.sessionId(), first.climbedOn(), first.savedAt(), lines);
                        })
                .toList();
    }
}
