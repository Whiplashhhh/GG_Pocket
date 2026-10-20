package fr.gravelinesgrimp.pocket.auth.domain;

/** Names of what GG Pocket stores in a server session, besides the security context. */
public final class SessionAttributes {

    /** Public id of the session in "Mes appareils" (the real session id stays secret). */
    public static final String DEVICE_ID = "gg.device.id";

    public static final String BROWSER = "gg.device.browser";
    public static final String PLATFORM = "gg.device.platform";

    /** "Rester connecté" was checked: the cookie outlives the browser. */
    public static final String REMEMBERED = "gg.remembered";

    /** Last time the long-lived cookie was sent again (epoch milliseconds). */
    public static final String COOKIE_REFRESHED_AT = "gg.cookie.refreshedAt";

    private SessionAttributes() {}
}
