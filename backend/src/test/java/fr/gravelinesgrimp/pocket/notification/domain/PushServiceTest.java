package fr.gravelinesgrimp.pocket.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Only Apple's, Google's and Mozilla's push services: the server calls nothing else (D-130). */
class PushServiceTest {

    @ParameterizedTest
    @ValueSource(
            strings = {
                "https://web.push.apple.com/QGuQyavXutnMH",
                "https://fcm.googleapis.com/fcm/send/dpJ2:APA91b",
                "https://updates.push.services.mozilla.com/wpush/v2/gAAAA",
                "https://FCM.googleapis.com:443/fcm/send/x"
            })
    void knowsTheThreePushServices(String endpoint) {
        assertThat(PushService.of(endpoint)).isPresent();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "http://fcm.googleapis.com/fcm/send/x",
                "https://fcm.googleapis.com.evil.example/x",
                "https://evil.example/fcm.googleapis.com",
                "https://user@fcm.googleapis.com/x",
                "https://fcm.googleapis.com:8443/x",
                "https://localhost/x",
                "https://169.254.169.254/latest",
                "not a url",
                ""
            })
    void refusesAnythingElse(String endpoint) {
        assertThat(PushService.of(endpoint)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://web:8081", "http://web:8081/"})
    void goesThroughTheRelayWithItsPathAndQuery(String relay) {
        assertThat(PushService.APPLE.throughRelay(relay, "https://web.push.apple.com/QGuQ/x?y=1"))
                .isEqualTo("http://web:8081/apple/QGuQ/x?y=1");
        assertThat(PushService.GOOGLE.origin()).isEqualTo("https://fcm.googleapis.com");
    }
}
