package fr.gravelinesgrimp.pocket.notification.application;

import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.notification.domain.NotificationKind;
import fr.gravelinesgrimp.pocket.notification.infra.NotificationQueries;
import fr.gravelinesgrimp.pocket.notification.infra.NotificationRow;
import fr.gravelinesgrimp.pocket.session.application.SessionAudience;
import fr.gravelinesgrimp.pocket.session.domain.ReactionKind;
import fr.gravelinesgrimp.pocket.session.infra.ClimbingSessionRepository;
import fr.gravelinesgrimp.pocket.social.application.Friendships;
import fr.gravelinesgrimp.pocket.topo.application.MemberRefView;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The bell (club feedback, 2026-11-16, D-128): what happened to me lately, newest first, and how
 * much of it is new since I last opened it. Computed each time from the tables (no stored
 * notification): a month back, at most {@value #LIMIT} events.
 */
@Service
public class NotificationService {

    static final int LIMIT = 50;
    static final Duration HORIZON = Duration.ofDays(30);

    /** One event; the session fields are null for a friendship. */
    public record NotificationView(
            String id,
            NotificationKind kind,
            Instant at,
            MemberRefView actor,
            UUID sessionId,
            LocalDate climbedOn,
            ReactionKind reaction,
            String excerpt,
            boolean unread) {}

    public record NotificationsView(List<NotificationView> items, int unreadCount) {}

    private final NotificationQueries queries;
    private final MemberRepository members;
    private final Friendships friendships;
    private final ClimbingSessionRepository sessions;
    private final SessionAudience audience;
    private final Clock clock;

    public NotificationService(
            NotificationQueries queries,
            MemberRepository members,
            Friendships friendships,
            ClimbingSessionRepository sessions,
            SessionAudience audience,
            Clock clock) {
        this.queries = queries;
        this.members = members;
        this.friendships = friendships;
        this.sessions = sessions;
        this.audience = audience;
        this.clock = clock;
    }

    /**
     * Mine only: no id to pass, nothing of anyone else's to guess. An answer under a session I can
     * no longer read (friendship over, session made private) is left out, and so is anything done
     * by an account that is no longer active.
     */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional(readOnly = true)
    public NotificationsView mine(UUID memberId) {
        var friendIds = new ArrayList<UUID>();
        friendIds.add(memberId);
        members.findAllById(friendships.friendIdsOf(memberId)).stream()
                .filter(Member::isActive)
                .map(Member::id)
                .forEach(friendIds::add);
        var rows =
                queries.latest(memberId, friendIds, clock.instant().minus(HORIZON), LIMIT).stream()
                        .filter(row -> readable(memberId, row))
                        .toList();
        Map<UUID, Member> actors =
                members
                        .findAllById(
                                rows.stream().map(NotificationRow::actorId).distinct().toList())
                        .stream()
                        .filter(Member::isActive)
                        .collect(Collectors.toMap(Member::id, Function.identity()));
        var seenAt = queries.seenAt(memberId).orElse(null);
        var items =
                rows.stream()
                        .filter(row -> actors.containsKey(row.actorId()))
                        .map(row -> toView(row, actors.get(row.actorId()), seenAt))
                        .toList();
        return new NotificationsView(
                items, (int) items.stream().filter(NotificationView::unread).count());
    }

    /** I opened the bell: what is there is no longer new. */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional
    public void markSeen(UUID memberId) {
        queries.markSeen(memberId, clock.instant());
    }

    /**
     * Only an answer under someone else's session needs a check; the rest is mine or a friend's.
     */
    private boolean readable(UUID memberId, NotificationRow row) {
        if (row.kind() != NotificationKind.REPLY
                || Objects.equals(row.sessionOwnerId(), memberId)) {
            return true;
        }
        return sessions.findById(row.sessionId())
                .map(session -> audience.canRead(memberId, session))
                .orElse(false);
    }

    private static NotificationView toView(NotificationRow row, Member actor, Instant seenAt) {
        return new NotificationView(
                row.kind().name() + ":" + row.sourceId(),
                row.kind(),
                row.at(),
                MemberRefView.of(actor),
                row.sessionId(),
                row.climbedOn(),
                row.reaction() == null ? null : ReactionKind.valueOf(row.reaction()),
                row.excerpt(),
                seenAt == null || row.at().isAfter(seenAt));
    }
}
