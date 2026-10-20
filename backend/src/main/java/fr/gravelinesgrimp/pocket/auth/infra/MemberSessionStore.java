package fr.gravelinesgrimp.pocket.auth.infra;

import fr.gravelinesgrimp.pocket.auth.domain.DeviceDescription;
import fr.gravelinesgrimp.pocket.auth.domain.SessionAttributes;
import fr.gravelinesgrimp.pocket.auth.domain.Sha256;
import fr.gravelinesgrimp.pocket.auth.domain.StoredSession;

import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * The server sessions of a member, found through the principal name (the member id) indexed by
 * Spring Session JDBC.
 */
@Component
public class MemberSessionStore {

    private final FindByIndexNameSessionRepository<? extends Session> sessions;

    public MemberSessionStore(FindByIndexNameSessionRepository<? extends Session> sessions) {
        this.sessions = sessions;
    }

    /** Most recently active first; expired sessions are not returned. */
    public List<StoredSession> findByMember(UUID memberId) {
        return sessions.findByPrincipalName(memberId.toString()).values().stream()
                .filter(session -> !session.isExpired())
                .map(MemberSessionStore::toStoredSession)
                .sorted(Comparator.comparing(StoredSession::lastActiveAt).reversed())
                .toList();
    }

    public void delete(String sessionId) {
        sessions.deleteById(sessionId);
    }

    /** Closes every session of the member, except {@code keptSessionId} if not null. */
    public void deleteAllOf(UUID memberId, String keptSessionId) {
        sessions.findByPrincipalName(memberId.toString())
                .keySet()
                .forEach(
                        sessionId -> {
                            if (!sessionId.equals(keptSessionId)) {
                                sessions.deleteById(sessionId);
                            }
                        });
    }

    /**
     * Same, once the current transaction has committed: after a password change, a sign-in that
     * slipped in with the old password before the commit cannot outlive the change.
     */
    public void deleteAllOfAfterCommit(UUID memberId, String keptSessionId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteAllOf(memberId, keptSessionId);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        deleteAllOf(memberId, keptSessionId);
                    }
                });
    }

    private static StoredSession toStoredSession(Session session) {
        String deviceId = session.getAttribute(SessionAttributes.DEVICE_ID);
        String browser = session.getAttribute(SessionAttributes.BROWSER);
        String platform = session.getAttribute(SessionAttributes.PLATFORM);
        var device =
                browser == null || platform == null
                        ? DeviceDescription.UNKNOWN
                        : new DeviceDescription(
                                DeviceDescription.Browser.valueOf(browser),
                                DeviceDescription.Platform.valueOf(platform));
        return new StoredSession(
                session.getId(),
                // Sessions always get a device id at sign-in; never fall back to the secret id.
                deviceId == null ? Sha256.hex(session.getId()) : deviceId,
                device,
                session.getCreationTime(),
                session.getLastAccessedTime());
    }
}
