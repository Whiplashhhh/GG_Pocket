package fr.gravelinesgrimp.pocket.notification.infra;

import fr.gravelinesgrimp.pocket.notification.domain.PushGateway;
import fr.gravelinesgrimp.pocket.notification.domain.PushService;
import fr.gravelinesgrimp.pocket.notification.domain.PushSubscription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Sends to the push services over HTTP (RFC 8030), through the web container's relay when one is
 * set (prod and dev: the API has no Internet access, D-130), straight otherwise (local runs).
 */
public class HttpPushGateway implements PushGateway {

    private static final Logger log = LoggerFactory.getLogger(HttpPushGateway.class);

    /** A day: a message about a reaction is stale after that. */
    static final int TTL_SECONDS = 24 * 60 * 60;

    private final HttpClient http;
    private final String relay;

    /**
     * @param relay {@code http://web:8081}, or blank to call the push services directly
     */
    public HttpPushGateway(String relay) {
        this.http =
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .followRedirects(HttpClient.Redirect.NEVER)
                        .build();
        this.relay = relay == null ? "" : relay.strip();
    }

    @Override
    public Outcome send(
            PushSubscription device, PushService service, byte[] body, String authorization) {
        var target =
                relay.isEmpty()
                        ? device.endpoint()
                        : service.throughRelay(relay, device.endpoint());
        var request =
                HttpRequest.newBuilder(URI.create(target))
                        .timeout(Duration.ofSeconds(15))
                        .header("TTL", Integer.toString(TTL_SECONDS))
                        .header("Urgency", "normal")
                        .header("Content-Encoding", "aes128gcm")
                        .header("Content-Type", "application/octet-stream")
                        .header("Authorization", authorization)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                        .build();
        try {
            var status = http.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
            if (status >= 200 && status < 300) {
                return Outcome.DELIVERED;
            }
            if (status == 404 || status == 410) {
                return Outcome.GONE;
            }
            // The endpoint is a secret of the device: the service's name is enough in the logs.
            log.warn("Push to {} refused with HTTP {}", service, status);
            return Outcome.FAILED;
        } catch (IOException e) {
            log.warn("Push to {} failed: {}", service, e.toString());
            return Outcome.FAILED;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Outcome.FAILED;
        }
    }
}
