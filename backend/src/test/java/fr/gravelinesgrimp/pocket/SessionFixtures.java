package fr.gravelinesgrimp.pocket;

import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.session.domain.AscentDraft;
import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;
import fr.gravelinesgrimp.pocket.session.domain.SessionDraft;
import fr.gravelinesgrimp.pocket.session.domain.SessionVisibility;
import fr.gravelinesgrimp.pocket.session.infra.ClimbingSessionRepository;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;
import fr.gravelinesgrimp.pocket.topo.domain.Route;

import org.springframework.boot.test.context.TestComponent;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Sessions written straight in the database, on any day (frozen months included). They skip the
 * service: no automatic "je sais la faire" marks.
 */
@TestComponent
public class SessionFixtures {

    private final ClimbingSessionRepository sessions;

    public SessionFixtures(ClimbingSessionRepository sessions) {
        this.sessions = sessions;
    }

    public ClimbingSession log(
            Member climber, LocalDate day, Integer feeling, AscentDraft... ascents) {
        return log(climber, day, feeling, SessionVisibility.FRIENDS, ascents);
    }

    public ClimbingSession log(
            Member climber,
            LocalDate day,
            Integer feeling,
            SessionVisibility visibility,
            AscentDraft... ascents) {
        var draft = new SessionDraft(day, feeling, null, visibility, List.of(ascents));
        return sessions.saveAndFlush(
                ClimbingSession.create(climber.id(), UUID.randomUUID(), draft));
    }

    public static AscentDraft topped(Route route, ClimbMode mode) {
        return new AscentDraft(route.id(), mode, true, false, 1, false, null);
    }

    public static AscentDraft clean(Route route, ClimbMode mode) {
        return new AscentDraft(route.id(), mode, true, true, 0, false, null);
    }

    /** Topped clean, with {@code belayer} on the rope. */
    public static AscentDraft belayed(Route route, ClimbMode mode, Member belayer) {
        return new AscentDraft(route.id(), mode, true, true, 0, false, null, belayer.id());
    }

    public static AscentDraft tried(Route route, ClimbMode mode) {
        return new AscentDraft(route.id(), mode, false, false, 2, false, null);
    }
}
