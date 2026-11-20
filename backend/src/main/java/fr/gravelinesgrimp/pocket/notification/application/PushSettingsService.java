package fr.gravelinesgrimp.pocket.notification.application;

import fr.gravelinesgrimp.pocket.notification.config.PushKeys;
import fr.gravelinesgrimp.pocket.notification.domain.PushPreferences;
import fr.gravelinesgrimp.pocket.notification.domain.PushService;
import fr.gravelinesgrimp.pocket.notification.domain.PushSubscription;
import fr.gravelinesgrimp.pocket.notification.domain.WebPush;
import fr.gravelinesgrimp.pocket.notification.infra.PushStore;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * My push settings (D-128, D-130): the devices that receive my notifications and the kinds I want.
 * Everything is mine: a device I remove is only ever one of mine.
 */
@Service
public class PushSettingsService {

    /** Phones, tablet, computer: more than that is an old device nobody removed. */
    static final int MAX_DEVICES = 10;

    /**
     * @param publicKey the server's VAPID key, null when push is off
     * @param thisDevice whether the device asked about receives my notifications
     */
    public record PushSettingsView(
            String publicKey, PushPreferences preferences, boolean thisDevice) {}

    private final PushKeys keys;
    private final PushStore store;
    private final Clock clock;

    public PushSettingsService(PushKeys keys, PushStore store, Clock clock) {
        this.keys = keys;
        this.store = store;
        this.clock = clock;
    }

    /**
     * @param endpoint the device asking, if it has a push subscription
     */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional(readOnly = true)
    public PushSettingsView settings(UUID memberId, String endpoint) {
        return new PushSettingsView(
                keys.enabled() ? keys.vapid().publicKeyBase64() : null,
                store.preferencesOf(memberId).orElse(PushPreferences.DEFAULTS),
                endpoint != null && !endpoint.isBlank() && store.hasDevice(memberId, endpoint));
    }

    /**
     * @throws BusinessException {@code NOT_FOUND} when push is off, {@code VALIDATION_FAILED} for a
     *     push service other than Apple's, Google's or Mozilla's, or keys that are not keys
     */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional
    public void addDevice(UUID memberId, PushSubscription device) {
        if (!keys.enabled()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Push is off");
        }
        if (PushService.of(device.endpoint()).isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Unknown push service");
        }
        try {
            WebPush.publicKey(WebPush.decode(device.p256dh()));
            if (WebPush.decode(device.auth()).length != 16) {
                throw new IllegalArgumentException("auth is 16 bytes");
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Not push keys");
        }
        store.subscribe(memberId, device, clock.instant());
        store.keepLatest(memberId, MAX_DEVICES);
    }

    /** Nothing happens for a device that is not mine. */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional
    public void removeDevice(UUID memberId, String endpoint) {
        store.unsubscribe(memberId, endpoint);
    }

    @PreAuthorize("hasRole('TOURIST')")
    @Transactional
    public PushPreferences savePreferences(UUID memberId, PushPreferences preferences) {
        store.savePreferences(memberId, preferences);
        return preferences;
    }
}
