package fr.gravelinesgrimp.pocket.notification.domain;

/**
 * One device of a member: where its push service waits for messages, and the keys to encrypt them
 * for it ({@code p256dh}, {@code auth}: base64url, as the browser gives them).
 */
public record PushSubscription(String endpoint, String p256dh, String auth) {}
