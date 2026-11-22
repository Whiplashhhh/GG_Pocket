package fr.gravelinesgrimp.pocket.notification.application;

import static java.nio.charset.StandardCharsets.UTF_8;

import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.notification.config.PushKeys;
import fr.gravelinesgrimp.pocket.notification.config.PushRunner;
import fr.gravelinesgrimp.pocket.notification.domain.Json;
import fr.gravelinesgrimp.pocket.notification.domain.NotificationEvent;
import fr.gravelinesgrimp.pocket.notification.domain.NotificationKind;
import fr.gravelinesgrimp.pocket.notification.domain.PushGateway;
import fr.gravelinesgrimp.pocket.notification.domain.PushPreferences;
import fr.gravelinesgrimp.pocket.notification.domain.PushService;
import fr.gravelinesgrimp.pocket.notification.domain.PushSubscription;
import fr.gravelinesgrimp.pocket.notification.domain.WebPush;
import fr.gravelinesgrimp.pocket.notification.infra.PushStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

/**
 * Sends a {@link NotificationEvent} to the recipient's devices (D-128, D-130), once what caused it
 * is committed, off the request's thread: if the member wants that kind, every device of theirs
 * gets the message, encrypted for it. A device its push service says is gone is forgotten. The
 * message carries data, not text: the app's service worker words it (texts in the i18n file).
 */
@Component
public class PushDelivery {

    private static final Logger log = LoggerFactory.getLogger(PushDelivery.class);

    /** VAPID tokens may live up to a day; half of it is plenty for one message. */
    static final Duration TOKEN_LIFETIME = Duration.ofHours(12);

    private record Plan(List<PushSubscription> devices, byte[] payload) {}

    private final PushKeys keys;
    private final PushStore store;
    private final PushGateway gateway;
    private final PushRunner runner;
    private final MemberRepository members;
    private final TransactionTemplate reads;
    private final TransactionTemplate writes;
    private final Clock clock;

    public PushDelivery(
            PushKeys keys,
            PushStore store,
            PushGateway gateway,
            PushRunner runner,
            MemberRepository members,
            PlatformTransactionManager transactionManager,
            Clock clock) {
        this.keys = keys;
        this.store = store;
        this.gateway = gateway;
        this.runner = runner;
        this.members = members;
        // Their own transactions: after a commit, the finished one may still be bound to the
        // thread.
        this.reads = new TransactionTemplate(transactionManager);
        this.reads.setReadOnly(true);
        this.reads.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.writes = new TransactionTemplate(transactionManager);
        this.writes.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.clock = clock;
    }

    @TransactionalEventListener
    public void on(NotificationEvent event) {
        if (!keys.enabled() || event.recipientId().equals(event.actorId())) {
            return;
        }
        runner.run(() -> deliver(event));
    }

    private void deliver(NotificationEvent event) {
        try {
            var plan = reads.execute(status -> plan(event));
            if (plan == null) {
                return;
            }
            for (var device : plan.devices()) {
                send(device, plan.payload());
            }
        } catch (RuntimeException e) {
            // A push is a bonus: whatever goes wrong here, what caused it is saved.
            log.warn("Push not sent: {}", e.toString());
        }
    }

    private Plan plan(NotificationEvent event) {
        var preferences = store.preferencesOf(event.recipientId()).orElse(PushPreferences.DEFAULTS);
        if (!preferences.allows(event.kind())) {
            return null;
        }
        var devices = store.devicesOf(event.recipientId());
        var recipient = members.findById(event.recipientId()).filter(Member::isActive);
        var actor = members.findById(event.actorId()).filter(Member::isActive);
        if (devices.isEmpty() || recipient.isEmpty() || actor.isEmpty()) {
            return null;
        }
        return new Plan(devices, payload(event, actor.get().nickname()).getBytes(UTF_8));
    }

    private void send(PushSubscription device, byte[] payload) {
        var service = PushService.of(device.endpoint());
        if (service.isEmpty()) {
            writes.executeWithoutResult(status -> store.forget(device.endpoint()));
            return;
        }
        var body =
                WebPush.encrypt(
                        payload, WebPush.decode(device.p256dh()), WebPush.decode(device.auth()));
        var authorization =
                WebPush.vapidAuthorization(
                        keys.vapid(),
                        service.get().origin(),
                        keys.subject(),
                        clock.instant().plus(TOKEN_LIFETIME));
        if (gateway.send(device, service.get(), body, authorization) == PushGateway.Outcome.GONE) {
            writes.executeWithoutResult(status -> store.forget(device.endpoint()));
        }
    }

    /** What the service worker needs to word the message, and where a tap leads. */
    static String payload(NotificationEvent event, String nickname) {
        String url;
        if (event.sessionId() != null) {
            url = "/seances/" + event.sessionId();
        } else if (event.kind() == NotificationKind.FRIEND_REQUEST) {
            url = "/profil";
        } else {
            url = "/membres/" + event.actorId();
        }
        return "{\"kind\":"
                + Json.string(event.kind().name())
                + ",\"nickname\":"
                + Json.string(nickname)
                + ",\"climbedOn\":"
                + Json.string(event.climbedOn() == null ? null : event.climbedOn().toString())
                + ",\"reaction\":"
                + Json.string(event.reaction())
                + ",\"excerpt\":"
                + Json.string(event.excerpt())
                + ",\"url\":"
                + Json.string(url)
                + "}";
    }
}
