package fr.gravelinesgrimp.pocket.notification.domain;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;

/**
 * The push services a device may use (D-130): Apple (Safari, iPhone), Google (Chrome, Android) and
 * Mozilla (Firefox). Anything else is refused: the server only ever calls these three, through the
 * relay of the web container (the API itself has no Internet access).
 */
public enum PushService {
    APPLE("web.push.apple.com", "apple"),
    GOOGLE("fcm.googleapis.com", "fcm"),
    MOZILLA("updates.push.services.mozilla.com", "mozilla");

    /** Endpoints are a few hundred characters; the column holds 1000. */
    public static final int MAX_ENDPOINT_LENGTH = 1000;

    private final String host;
    private final String relayPath;

    PushService(String host, String relayPath) {
        this.host = host;
        this.relayPath = relayPath;
    }

    /** The service of an endpoint: HTTPS on the default port, one of the three hosts, or none. */
    public static Optional<PushService> of(String endpoint) {
        if (endpoint == null || endpoint.length() > MAX_ENDPOINT_LENGTH) {
            return Optional.empty();
        }
        URI uri;
        try {
            uri = URI.create(endpoint);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        if (!"https".equals(uri.getScheme())
                || uri.getHost() == null
                || uri.getRawUserInfo() != null
                || (uri.getPort() != -1 && uri.getPort() != 443)) {
            return Optional.empty();
        }
        var found = uri.getHost().toLowerCase(Locale.ROOT);
        for (var service : values()) {
            if (service.host.equals(found)) {
                return Optional.of(service);
            }
        }
        return Optional.empty();
    }

    /** The audience of the VAPID token: the origin of the push service. */
    public String origin() {
        return "https://" + host;
    }

    /**
     * Where to send through the relay ({@code http://web:8081}): {@code <relay>/apple/<path>}. The
     * endpoint is checked by {@link #of} first.
     */
    public String throughRelay(String relay, String endpoint) {
        var uri = URI.create(endpoint);
        var rest = uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
        return relay.replaceAll("/+$", "") + "/" + relayPath + rest;
    }
}
