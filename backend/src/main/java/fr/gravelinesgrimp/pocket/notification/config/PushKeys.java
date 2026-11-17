package fr.gravelinesgrimp.pocket.notification.config;

import fr.gravelinesgrimp.pocket.notification.domain.WebPush;

/**
 * The server's VAPID keys and contact (D-130), or nothing: push off.
 *
 * @param vapid null when push is off
 */
public record PushKeys(WebPush.VapidKeys vapid, String subject) {

    public boolean enabled() {
        return vapid != null;
    }
}
