package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;
import fr.gravelinesgrimp.pocket.session.infra.ClimbingSessionRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Loads a session for its climber. Someone else's session is "not found", exactly like a missing
 * one: the answer never tells that it exists.
 */
@Component
public class OwnedSessions {

    private final ClimbingSessionRepository sessions;

    public OwnedSessions(ClimbingSessionRepository sessions) {
        this.sessions = sessions;
    }

    /**
     * @throws BusinessException {@code NOT_FOUND}
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public ClimbingSession load(UUID memberId, UUID sessionId) {
        return sessions.findWithAscentsById(sessionId)
                .filter(session -> session.memberId().equals(memberId))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No session"));
    }
}
