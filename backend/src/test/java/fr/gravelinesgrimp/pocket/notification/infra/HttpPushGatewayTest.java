package fr.gravelinesgrimp.pocket.notification.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;

import fr.gravelinesgrimp.pocket.notification.domain.PushGateway.Outcome;
import fr.gravelinesgrimp.pocket.notification.domain.PushService;
import fr.gravelinesgrimp.pocket.notification.domain.PushSubscription;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

/** What reaches the relay of the web container (D-130), played here by a small HTTP server. */
class HttpPushGatewayTest {

    private HttpServer relay;
    private final AtomicReference<String> path = new AtomicReference<>();
    private final AtomicReference<Headers> headers = new AtomicReference<>();
    private int status = 201;

    @BeforeEach
    void start() throws IOException {
        relay = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        relay.createContext(
                "/",
                exchange -> {
                    path.set(exchange.getRequestURI().toString());
                    headers.set(exchange.getRequestHeaders());
                    exchange.getRequestBody().readAllBytes();
                    exchange.sendResponseHeaders(status, -1);
                    exchange.close();
                });
        relay.start();
    }

    @AfterEach
    void stop() {
        relay.stop(0);
    }

    @Test
    void thePlainHttpRelayGetsNoUpgradeToHttp2() {
        var outcome = send("https://web.push.apple.com/QMb29hifo0oO9");

        assertThat(outcome).isEqualTo(Outcome.DELIVERED);
        assertThat(path.get()).isEqualTo("/apple/QMb29hifo0oO9");
        // The relay passed "Upgrade: h2c" on to Apple, which answered with an error (HTTP 502).
        assertThat(headers.get().containsKey("Upgrade")).isFalse();
        assertThat(headers.get().containsKey("Http2-settings")).isFalse();
        assertThat(headers.get().getFirst("Content-encoding")).isEqualTo("aes128gcm");
        assertThat(headers.get().getFirst("Ttl")).isEqualTo("86400");
        assertThat(headers.get().getFirst("Authorization")).isEqualTo("vapid t=x, k=y");
    }

    @Test
    void aDeviceGoneIsToldApart() {
        status = 410;

        assertThat(send("https://web.push.apple.com/gone")).isEqualTo(Outcome.GONE);
    }

    private Outcome send(String endpoint) {
        var gateway = new HttpPushGateway("http://127.0.0.1:" + relay.getAddress().getPort() + "/");
        return gateway.send(
                new PushSubscription(endpoint, "p256dh", "auth"),
                PushService.APPLE,
                new byte[] {1, 2, 3},
                "vapid t=x, k=y");
    }
}
