package fr.gravelinesgrimp.pocket.club.application;

import fr.gravelinesgrimp.pocket.admin.application.AuditLog;
import fr.gravelinesgrimp.pocket.admin.domain.AuditAction;
import fr.gravelinesgrimp.pocket.admin.domain.AuditTargetType;
import fr.gravelinesgrimp.pocket.badge.application.BadgeAwards;
import fr.gravelinesgrimp.pocket.club.domain.ClosedDay;
import fr.gravelinesgrimp.pocket.club.domain.ClosedDayRange;
import fr.gravelinesgrimp.pocket.club.infra.ClosedDayRepository;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The days the gym is closed (D-116): set by the board and the admin, shown on every planning.
 * Closing or reopening days replays the badges of the climbers of those months ("Mois complet"),
 * and goes to the audit log.
 */
@Service
public class ClosedDayService {

    public record ClosedDayView(LocalDate date, String label) {}

    private final ClosedDayRepository closedDays;
    private final BadgeAwards badges;
    private final MemberRepository members;
    private final AuditLog auditLog;
    private final Clock clock;

    public ClosedDayService(
            ClosedDayRepository closedDays,
            BadgeAwards badges,
            MemberRepository members,
            AuditLog auditLog,
            Clock clock) {
        this.closedDays = closedDays;
        this.badges = badges;
        this.members = members;
        this.auditLog = auditLog;
        this.clock = clock;
    }

    /** Every closed day, the earliest first. */
    @PreAuthorize("hasRole('BOARD')")
    @Transactional(readOnly = true)
    public List<ClosedDayView> list() {
        return closedDays.findAllByOrderByClosedOnAsc().stream()
                .map(ClosedDayService::toView)
                .toList();
    }

    /**
     * Closes every day from {@code from} to {@code to}; a day already closed takes the new label.
     *
     * @param to null for the single day {@code from}
     * @return every closed day, the earliest first
     */
    @PreAuthorize("hasRole('BOARD')")
    @Transactional
    public List<ClosedDayView> close(UUID actorId, LocalDate from, LocalDate to, String label) {
        var actor = load(actorId);
        var range = ClosedDayRange.of(from, to, ClubTime.today(clock));
        var normalized = ClosedDay.normalizeLabel(label);
        if (normalized != null && normalized.length() > ClosedDay.MAX_LABEL_LENGTH) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Label too long");
        }
        var existing =
                closedDays
                        .findAllByClosedOnBetweenOrderByClosedOnAsc(range.from(), range.to())
                        .stream()
                        .collect(Collectors.toMap(ClosedDay::closedOn, Function.identity()));
        for (var day : range.days()) {
            var closed = existing.get(day);
            if (closed == null) {
                closedDays.save(new ClosedDay(day, normalized));
            } else {
                closed.relabel(normalized);
            }
        }
        closedDays.flush();
        auditLog.record(
                actor,
                AuditAction.CLUB_DAYS_CLOSED,
                AuditTargetType.CLOSED_DAY,
                null,
                range.describe(),
                null,
                normalized);
        badges.refreshMonthsOf(range.from(), range.to());
        return list();
    }

    /**
     * Reopens every closed day from {@code from} to {@code to}; nothing to reopen is no error.
     *
     * @param to null for the single day {@code from}
     */
    @PreAuthorize("hasRole('BOARD')")
    @Transactional
    public void reopen(UUID actorId, LocalDate from, LocalDate to) {
        var actor = load(actorId);
        var range = ClosedDayRange.of(from, to, ClubTime.today(clock));
        var closed =
                closedDays.findAllByClosedOnBetweenOrderByClosedOnAsc(range.from(), range.to());
        if (closed.isEmpty()) {
            return;
        }
        closedDays.deleteAll(closed);
        closedDays.flush();
        auditLog.record(
                actor,
                AuditAction.CLUB_DAYS_REOPENED,
                AuditTargetType.CLOSED_DAY,
                null,
                range.describe(),
                closed.size() + " day(s)",
                null);
        badges.refreshMonthsOf(range.from(), range.to());
    }

    private Member load(UUID memberId) {
        return members.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No member"));
    }

    private static ClosedDayView toView(ClosedDay day) {
        return new ClosedDayView(day.closedOn(), day.label());
    }
}
