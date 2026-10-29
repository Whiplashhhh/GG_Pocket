package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;
import fr.gravelinesgrimp.pocket.session.domain.SessionCount;
import fr.gravelinesgrimp.pocket.session.domain.SessionVisibility;
import fr.gravelinesgrimp.pocket.session.infra.AscentRepository;
import fr.gravelinesgrimp.pocket.session.infra.ClimbingSessionRepository;
import fr.gravelinesgrimp.pocket.social.application.Friendships;
import fr.gravelinesgrimp.pocket.topo.application.MemberRefView;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The belayer's side (D-101): the sessions where someone named me as their belayer, and taking my
 * name back. Naming me gives me no right on the session: I see who climbed and when, and I open it
 * only if I could anyway.
 */
@Service
public class BelayService {

    private final ClimbingSessionRepository sessions;
    private final AscentRepository ascents;
    private final MemberRepository members;
    private final Friendships friendships;
    private final Clock clock;

    public BelayService(
            ClimbingSessionRepository sessions,
            AscentRepository ascents,
            MemberRepository members,
            Friendships friendships,
            Clock clock) {
        this.sessions = sessions;
        this.ascents = ascents;
        this.members = members;
        this.friendships = friendships;
        this.clock = clock;
    }

    /** Latest day first, with the cursor of the sessions. */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional(readOnly = true)
    public BelayView.Page mine(UUID me, String cursor, int size) {
        var limit = Math.clamp(size, 1, SessionService.MAX_PAGE_SIZE);
        List<ClimbingSession> found;
        if (cursor == null || cursor.isBlank()) {
            found = sessions.findLatestBelayedBy(me, limit + 1);
        } else {
            var position = SessionService.Cursor.decode(cursor);
            found =
                    sessions.findBelayedByBefore(
                            me,
                            position.climbedOn(),
                            position.createdAt(),
                            position.id(),
                            limit + 1);
        }
        var page = found.stream().limit(limit).toList();
        if (page.isEmpty()) {
            return new BelayView.Page(List.of(), null);
        }
        var sessionIds = page.stream().map(ClimbingSession::id).toList();
        Map<UUID, Long> routeCounts =
                ascents.countBelayedPerSession(me, sessionIds).stream()
                        .collect(Collectors.toMap(SessionCount::sessionId, SessionCount::count));
        // The foreign key guarantees that every climber exists.
        Map<UUID, Member> climbers =
                members
                        .findAllById(
                                page.stream()
                                        .map(ClimbingSession::memberId)
                                        .collect(Collectors.toSet()))
                        .stream()
                        .collect(Collectors.toMap(Member::id, Function.identity()));
        var friends = new HashSet<>(friendships.friendIdsOf(me));
        var items =
                page.stream()
                        .map(
                                session -> {
                                    var climber = climbers.get(session.memberId());
                                    return new BelayView(
                                            session.id(),
                                            MemberRefView.of(climber),
                                            session.climbedOn(),
                                            routeCounts.getOrDefault(session.id(), 0L).intValue(),
                                            // What SessionAudience says, without a query per line.
                                            session.visibility() == SessionVisibility.FRIENDS
                                                    && climber.isActive()
                                                    && friends.contains(climber.id()));
                                })
                        .toList();
        var last = page.getLast();
        var next =
                found.size() > limit
                        ? SessionService.Cursor.encode(
                                last.climbedOn(), last.createdAt(), last.id())
                        : null;
        return new BelayView.Page(items, next);
    }

    /**
     * Takes my name back from every route of a session, even in a frozen month: it changes nothing
     * to the climber's figures.
     *
     * @throws BusinessException {@code NOT_FOUND} (I am not named on that session)
     */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional
    public void withdraw(UUID me, UUID sessionId) {
        if (ascents.clearBelayer(sessionId, me, clock.instant()) == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Not your belay");
        }
    }
}
