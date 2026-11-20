package fr.gravelinesgrimp.pocket.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Push notifications (D-130), from {@code GG_PUSH_PUBLIC_KEY}, {@code GG_PUSH_PRIVATE_KEY}, {@code
 * GG_PUSH_SUBJECT} and {@code GG_PUSH_RELAY}. Without both keys, push is off: the app does not
 * offer it, and nothing is sent.
 *
 * @param publicKey VAPID public key, base64url (deploy/scripts/vapid-keys.sh)
 * @param privateKey VAPID private key, base64url: a secret, in the server's .env only
 * @param subject who to contact about this server: a {@code mailto:} or {@code https:} URL
 * @param relay the web container's push relay ({@code http://web:8081}); blank: straight out
 */
@ConfigurationProperties(prefix = "gg.push")
public record PushProperties(
        @DefaultValue("") String publicKey,
        @DefaultValue("") String privateKey,
        @DefaultValue("") String subject,
        @DefaultValue("") String relay) {

    public boolean enabled() {
        return !publicKey.isBlank() && !privateKey.isBlank();
    }
}
