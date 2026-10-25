package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.member.application.PrivateMediaAccess;
import fr.gravelinesgrimp.pocket.member.domain.MediaUsage;
import fr.gravelinesgrimp.pocket.session.infra.ClimbingSessionRepository;

import org.springframework.stereotype.Component;

import java.util.UUID;

/** A session photo is seen by whoever may read the session: its climber only, until phase 5. */
@Component
class SessionPhotoAccess implements PrivateMediaAccess {

    private final ClimbingSessionRepository sessions;

    SessionPhotoAccess(ClimbingSessionRepository sessions) {
        this.sessions = sessions;
    }

    @Override
    public MediaUsage usage() {
        return MediaUsage.SESSION_PHOTO;
    }

    @Override
    public boolean canRead(UUID readerId, UUID mediaId) {
        return sessions.findByPhotoId(mediaId)
                .map(session -> session.memberId().equals(readerId))
                .orElse(false);
    }
}
