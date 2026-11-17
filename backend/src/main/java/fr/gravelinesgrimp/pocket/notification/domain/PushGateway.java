package fr.gravelinesgrimp.pocket.notification.domain;

/** Hands an encrypted message to a device's push service (D-130). */
public interface PushGateway {

    enum Outcome {
        /** The push service took it. */
        DELIVERED,
        /** The device is gone (unsubscribed, app removed): forget it. */
        GONE,
        /** Anything else: logged, the next message will try again. */
        FAILED
    }

    Outcome send(PushSubscription device, PushService service, byte[] body, String authorization);
}
