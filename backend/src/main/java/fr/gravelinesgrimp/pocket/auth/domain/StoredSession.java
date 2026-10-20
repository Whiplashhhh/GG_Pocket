package fr.gravelinesgrimp.pocket.auth.domain;

import java.time.Instant;

/**
 * A server session of a member, as listed in "Mes appareils".
 *
 * @param sessionId the real session id: secret, never sent to the client
 * @param deviceId opaque public id of the session, chosen at sign-in
 */
public record StoredSession(
        String sessionId,
        String deviceId,
        DeviceDescription device,
        Instant signedInAt,
        Instant lastActiveAt) {}
