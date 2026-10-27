package fr.gravelinesgrimp.pocket.social.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

class FriendshipTest {

    private static final Instant NOW = Instant.parse("2026-10-27T18:30:00Z");

    private final UUID me = UUID.randomUUID();
    private final UUID other = UUID.randomUUID();

    @Test
    void aRequestIsPendingUntilTheAddresseeAcceptsIt() {
        var friendship = Friendship.request(me, other);

        assertThat(friendship.status()).isEqualTo(FriendshipStatus.PENDING);
        assertThat(friendship.acceptedAt()).isNull();
        assertThatThrownBy(() -> friendship.acceptBy(me, NOW))
                .isInstanceOf(IllegalStateException.class);

        friendship.acceptBy(other, NOW);
        assertThat(friendship.isAccepted()).isTrue();
        assertThat(friendship.acceptedAt()).isEqualTo(NOW);

        // Accepting twice keeps the first date.
        friendship.acceptBy(other, NOW.plusSeconds(60));
        assertThat(friendship.acceptedAt()).isEqualTo(NOW);
    }

    @Test
    void nobodyBefriendsThemselves() {
        assertThatThrownBy(() -> Friendship.request(me, me))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void eachSideSeesTheOther() {
        var friendship = Friendship.request(me, other);

        assertThat(friendship.otherThan(me)).isEqualTo(other);
        assertThat(friendship.otherThan(other)).isEqualTo(me);
        assertThatThrownBy(() -> friendship.otherThan(UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theStateDependsOnWhichSideIAm() {
        var friendship = Friendship.request(me, other);

        assertThat(FriendshipState.of(null, me)).isEqualTo(FriendshipState.NONE);
        assertThat(FriendshipState.of(friendship, me)).isEqualTo(FriendshipState.REQUESTED);
        assertThat(FriendshipState.of(friendship, other)).isEqualTo(FriendshipState.INCOMING);

        friendship.acceptBy(other, NOW);
        assertThat(FriendshipState.of(friendship, me)).isEqualTo(FriendshipState.FRIENDS);
        assertThat(FriendshipState.of(friendship, other)).isEqualTo(FriendshipState.FRIENDS);
    }
}
