package fr.gravelinesgrimp.pocket.session.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.function.BooleanSupplier;

/** Who else reads a session (spec §14, D-120). */
class SessionVisibilityTest {

    private static final BooleanSupplier YES = () -> true;
    private static final BooleanSupplier NO = () -> false;

    /** Fails the test if asked: the answer must not matter. */
    private static final BooleanSupplier NEVER_ASKED =
            () -> {
                throw new AssertionError("Asked for nothing");
            };

    @ParameterizedTest
    @CsvSource({
        // visibility, friend, in the club feed together, readable
        "PRIVATE, true,  true,  false",
        "FRIENDS, true,  false, true",
        "FRIENDS, false, true,  false",
        "CLUB,    true,  false, true",
        "CLUB,    false, true,  true",
        "CLUB,    false, false, false",
    })
    void whoElseReadsASession(
            SessionVisibility visibility, boolean friend, boolean together, boolean readable) {
        assertThat(visibility.readableBy(() -> friend, () -> together)).isEqualTo(readable);
    }

    @Test
    void aPrivateSessionAsksNothing() {
        assertThat(SessionVisibility.PRIVATE.readableBy(NEVER_ASKED, NEVER_ASKED)).isFalse();
    }

    @Test
    void theClubFeedNeverOpensASessionSharedWithFriendsOnly() {
        assertThat(SessionVisibility.FRIENDS.readableBy(NO, NEVER_ASKED)).isFalse();
    }

    @Test
    void aFriendReadsAClubSessionWithoutTakingPartInTheClubFeed() {
        assertThat(SessionVisibility.CLUB.readableBy(YES, NEVER_ASKED)).isTrue();
    }
}
