package fr.gravelinesgrimp.pocket.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.Signature;
import java.time.Instant;
import java.util.Arrays;

/** Checked against the example of RFC 8291 (appendix A), byte for byte. */
class WebPushTest {

    static final String SERVER_PUBLIC =
            "BP4z9KsN6nGRTbVYI_c7VJSPQTBtkgcy27mlmlMoZIIgDll6e3vCYLocInmYWAmS6TlzAC8wEqKK6PBru3jl7A8";
    static final String SERVER_PRIVATE = "yfWPiYE-n46HLnH0KqZOF1fJJU3MYrct3AELtAQ-oRw";
    static final String DEVICE_PUBLIC =
            "BCVxsr7N_eNgVRqvHtD0zTZsEc6-VV-JvLexhqUzORcxaOzi6-AYWXvTBHm4bjyPjs7Vd8pZGH6SRpkNtoIAiw4";
    static final String AUTH = "BTBZMqHH6r4Tts7J_aSIgg";
    static final String SALT = "DGv6ra1nlYgDCS1FRnbzlw";
    static final String MESSAGE = "When I grow up, I want to be a watermelon";
    static final String BODY =
            "DGv6ra1nlYgDCS1FRnbzlwAAEABBBP4z9KsN6nGRTbVYI_c7VJSPQTBtkgcy27mlmlMoZIIgDll6e3vCYLocInm"
                    + "YWAmS6TlzAC8wEqKK6PBru3jl7A_yl95bQpu6cVPTpK4Mqgkf1CXztLVBSt2Ks3oZwbuwXPXLWyouBWLV"
                    + "WGNWQexSgSxsj_Qulcy4a-fN";

    @Test
    void encryptsLikeTheRfcExample() {
        var server =
                new KeyPair(
                        WebPush.publicKey(WebPush.decode(SERVER_PUBLIC)),
                        WebPush.privateKey(WebPush.decode(SERVER_PRIVATE)));

        var body =
                WebPush.encrypt(
                        MESSAGE.getBytes(UTF_8),
                        WebPush.decode(DEVICE_PUBLIC),
                        WebPush.decode(AUTH),
                        server,
                        WebPush.decode(SALT));

        assertThat(WebPush.encode(body)).isEqualTo(BODY);
    }

    @Test
    void aFreshKeyAndSaltForEachMessage() {
        var device = WebPush.decode(DEVICE_PUBLIC);
        var auth = WebPush.decode(AUTH);

        var first = WebPush.encrypt(MESSAGE.getBytes(UTF_8), device, auth);
        var second = WebPush.encrypt(MESSAGE.getBytes(UTF_8), device, auth);

        assertThat(first).hasSameSizeAs(WebPush.decode(BODY));
        assertThat(Arrays.copyOf(first, 16)).isNotEqualTo(Arrays.copyOf(second, 16));
    }

    @Test
    void theVapidTokenIsSignedWithTheServerKey() throws Exception {
        var keys = WebPush.VapidKeys.of(SERVER_PUBLIC, SERVER_PRIVATE);

        var header =
                WebPush.vapidAuthorization(
                        keys,
                        "https://fcm.googleapis.com",
                        "mailto:club@example.org",
                        Instant.ofEpochSecond(1_800_000_000L));

        assertThat(header).startsWith("vapid t=").endsWith(", k=" + SERVER_PUBLIC);
        var token = header.substring("vapid t=".length(), header.indexOf(", k="));
        var parts = token.split("\\.");
        assertThat(new String(WebPush.decode(parts[1]), UTF_8))
                .isEqualTo(
                        "{\"aud\":\"https://fcm.googleapis.com\",\"exp\":1800000000,"
                                + "\"sub\":\"mailto:club@example.org\"}");
        var verifier = Signature.getInstance("SHA256withECDSAinP1363Format");
        verifier.initVerify(WebPush.publicKey(WebPush.decode(SERVER_PUBLIC)));
        verifier.update((parts[0] + "." + parts[1]).getBytes(US_ASCII));
        assertThat(verifier.verify(WebPush.decode(parts[2]))).isTrue();
    }

    @Test
    void refusesWhatIsNotAKey() {
        assertThatThrownBy(() -> WebPush.publicKey(new byte[64]))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> WebPush.VapidKeys.of(SERVER_PUBLIC, "c2hvcnQ"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(
                        () ->
                                WebPush.encrypt(
                                        new byte[WebPush.MAX_PLAINTEXT + 1],
                                        WebPush.decode(DEVICE_PUBLIC),
                                        WebPush.decode(AUTH)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
