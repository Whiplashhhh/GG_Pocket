package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;
import fr.gravelinesgrimp.pocket.session.infra.ClimbingSessionRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** A session its reader may read ({@link SessionAudience}), or "not found" whatever the reason. */
@Component
public class ReadableSessions {

    private final ClimbingSessionRepository sessions;
    private final SessionAudience audience;

    public ReadableSessions(ClimbingSessionRepository sessions, SessionAudience audience) {
        this.sessions = sessions;
        this.audience = audience;
    }

    /**
     * @throws BusinessException {@code NOT_FOUND}
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public ClimbingSession load(UUID readerId, UUID sessionId) {
        return sessions.findById(sessionId)
                .filter(found -> audience.canRead(readerId, found))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No session"));
    }
}
