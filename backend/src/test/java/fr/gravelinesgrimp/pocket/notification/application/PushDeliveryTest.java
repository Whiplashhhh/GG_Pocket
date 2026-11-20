package fr.gravelinesgrimp.pocket.notification.application;

import static org.assertj.core.api.Assertions.assertThat;

import fr.gravelinesgrimp.pocket.notification.domain.NotificationEvent;
import fr.gravelinesgrimp.pocket.notification.domain.NotificationKind;
import fr.gravelinesgrimp.pocket.notification.domain.PushPreferences;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

class PushDeliveryTest {

    private static final UUID SESSION = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ACTOR = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void theMessageCarriesDataAndWhereToGo() {
        var event =
                NotificationEvent.aboutSession(
                        UUID.randomUUID(),
                        NotificationKind.COMMENT,
                        ACTOR,
                        SESSION,
                        LocalDate.parse("2026-11-16"),
                        null,
                        "Le \"crux\" ?\nPropre");

        assertThat(PushDelivery.payload(event, "tom"))
                .isEqualTo(
                        "{\"kind\":\"COMMENT\",\"nickname\":\"tom\",\"climbedOn\":\"2026-11-16\","
                                + "\"reaction\":null,\"excerpt\":\"Le \\\"crux\\\" ?\\nPropre\","
                                + "\"url\":\"/seances/"
                                + SESSION
                                + "\"}");
    }

    @Test
    void aFriendshipLeadsToTheProfiles() {
        var asked =
                NotificationEvent.aboutFriendship(
                        UUID.randomUUID(), NotificationKind.FRIEND_REQUEST, ACTOR);
        var accepted =
                NotificationEvent.aboutFriendship(
                        UUID.randomUUID(), NotificationKind.FRIEND_ACCEPTED, ACTOR);

        assertThat(PushDelivery.payload(asked, "tom")).contains("\"url\":\"/profil\"");
        assertThat(PushDelivery.payload(accepted, "tom")).contains("\"url\":\"/membres/" + ACTOR);
    }

    @Test
    void longCommentsAreCut() {
        var event =
                NotificationEvent.aboutSession(
                        UUID.randomUUID(),
                        NotificationKind.REPLY,
                        ACTOR,
                        SESSION,
                        LocalDate.parse("2026-11-16"),
                        null,
                        "a".repeat(500));

        assertThat(event.excerpt()).hasSize(NotificationEvent.EXCERPT_LENGTH + 1).endsWith("…");
    }

    @Test
    void theDefaultsAreWillemsChoice() {
        var defaults = PushPreferences.DEFAULTS;

        assertThat(defaults.allows(NotificationKind.COMMENT)).isTrue();
        assertThat(defaults.allows(NotificationKind.REPLY)).isTrue();
        assertThat(defaults.allows(NotificationKind.FRIEND_REQUEST)).isTrue();
        assertThat(defaults.allows(NotificationKind.FRIEND_ACCEPTED)).isTrue();
        assertThat(defaults.allows(NotificationKind.REACTION)).isFalse();
        assertThat(defaults.allows(NotificationKind.FRIEND_SESSION)).isFalse();
    }
}
