package fr.gravelinesgrimp.pocket.auth.application;

import fr.gravelinesgrimp.pocket.auth.domain.DeviceDescription;
import fr.gravelinesgrimp.pocket.auth.domain.StoredSession;
import fr.gravelinesgrimp.pocket.auth.infra.MemberSessionStore;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** "Mes appareils": the open sessions of a member, and closing them. */
@Service
public class DeviceService {

    public record DeviceView(
            String id,
            DeviceDescription.Browser browser,
            DeviceDescription.Platform platform,
            Instant signedInAt,
            Instant lastActiveAt,
            boolean current) {}

    private final MemberSessionStore sessions;

    public DeviceService(MemberSessionStore sessions) {
        this.sessions = sessions;
    }

    public List<DeviceView> list(UUID memberId, String currentSessionId) {
        return sessions.findByMember(memberId).stream()
                .map(session -> toView(session, currentSessionId))
                .toList();
    }

    /**
     * Only the member's own sessions are searched: the id of someone else's device is simply not
     * found (404), whatever it is.
     */
    public void close(UUID memberId, String deviceId) {
        var session =
                sessions.findByMember(memberId).stream()
                        .filter(candidate -> candidate.deviceId().equals(deviceId))
                        .findFirst()
                        .orElseThrow(
                                () -> new BusinessException(ErrorCode.NOT_FOUND, "No such device"));
        sessions.delete(session.sessionId());
    }

    public void closeOthers(UUID memberId, String currentSessionId) {
        sessions.deleteAllOf(memberId, currentSessionId);
    }

    /** Used when an account is disabled. */
    public void closeAll(UUID memberId) {
        sessions.deleteAllOf(memberId, null);
    }

    private static DeviceView toView(StoredSession session, String currentSessionId) {
        return new DeviceView(
                session.deviceId(),
                session.device().browser(),
                session.device().platform(),
                session.signedInAt(),
                session.lastActiveAt(),
                session.sessionId().equals(currentSessionId));
    }
}
