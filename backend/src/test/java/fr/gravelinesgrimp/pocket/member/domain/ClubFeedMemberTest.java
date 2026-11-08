package fr.gravelinesgrimp.pocket.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

/**
 * "Je participe au fil du club" (D-120): off by default, and only for validated, active accounts.
 */
class ClubFeedMemberTest {

    private static final Instant NOW = Instant.parse("2026-11-07T18:00:00Z");

    @Test
    void nobodyTakesPartByDefault() {
        var member = validated();

        assertThat(member.inClubFeed()).isFalse();
        assertThat(member.isInClubFeed()).isFalse();
    }

    @Test
    void aValidatedActiveMemberTakesPartOnceTheyChooseToAndLeavesWhenTheyWant() {
        var member = validated();

        member.changeClubFeed(true);
        assertThat(member.isInClubFeed()).isTrue();

        member.changeClubFeed(false);
        assertThat(member.isInClubFeed()).isFalse();
    }

    @Test
    void anAccountAwaitingValidationNeverTakesPart() {
        var member =
                Member.awaitingValidation(
                        new MemberIdentity("alix", "Alix", "Moreau", null),
                        "hash",
                        LocalDate.of(2011, 3, 2),
                        false,
                        LicenceDeclaration.of(false, "Collègue de Lou, le jeudi."),
                        NOW);
        member.changeClubFeed(true);

        assertThat(member.isInClubFeed()).isFalse();
        member.validate(Role.TOURIST, NOW);
        assertThat(member.isInClubFeed()).isTrue();
    }

    @Test
    void aDisabledAccountDropsOutWithoutLosingTheChoice() {
        var member = validated();
        member.changeClubFeed(true);

        member.disable();
        assertThat(member.isInClubFeed()).isFalse();
        assertThat(member.inClubFeed()).isTrue();

        member.enable(NOW);
        assertThat(member.isInClubFeed()).isTrue();
    }

    private static Member validated() {
        return new Member(
                new MemberIdentity("lou", "Lou", "Martin", null),
                "hash",
                Role.CLIMBER,
                LocalDate.of(2012, 5, 1),
                true,
                NOW);
    }
}
