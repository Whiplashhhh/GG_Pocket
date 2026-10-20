package fr.gravelinesgrimp.pocket.member.domain;

import static fr.gravelinesgrimp.pocket.member.domain.Role.ADMIN;
import static fr.gravelinesgrimp.pocket.member.domain.Role.BOARD;
import static fr.gravelinesgrimp.pocket.member.domain.Role.CLIMBER;
import static fr.gravelinesgrimp.pocket.member.domain.Role.SETTER;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** The role matrix of spec §5, case by case. */
class MemberGovernanceTest {

    @Test
    void theBoardGivesAndTakesTheSetterRoleOnly() {
        assertThat(MemberGovernance.assignableRoles(BOARD, false, CLIMBER)).containsExactly(SETTER);
        assertThat(MemberGovernance.assignableRoles(BOARD, false, SETTER)).containsExactly(CLIMBER);
        assertThat(MemberGovernance.assignableRoles(BOARD, false, BOARD)).isEmpty();
        assertThat(MemberGovernance.assignableRoles(BOARD, false, ADMIN)).isEmpty();
    }

    @Test
    void theAdminGivesAnyRole() {
        assertThat(MemberGovernance.assignableRoles(ADMIN, false, CLIMBER))
                .containsExactlyInAnyOrder(SETTER, BOARD, ADMIN);
        assertThat(MemberGovernance.assignableRoles(ADMIN, false, ADMIN))
                .containsExactlyInAnyOrder(CLIMBER, SETTER, BOARD);
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void nobodyChangesTheirOwnRoleNorManagesTheirOwnAccount(Role role) {
        assertThat(MemberGovernance.assignableRoles(role, true, role)).isEmpty();
        assertThat(MemberGovernance.canManageAccount(role, true, role)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(
            value = Role.class,
            names = {"CLIMBER", "SETTER"})
    void climbersAndSettersManageNobody(Role actor) {
        for (var target : Role.values()) {
            assertThat(MemberGovernance.assignableRoles(actor, false, target)).isEmpty();
            assertThat(MemberGovernance.canManageAccount(actor, false, target)).isFalse();
        }
    }

    @Test
    void theBoardManagesTheAccountsOfClimbersAndSettersOnly() {
        assertThat(MemberGovernance.canManageAccount(BOARD, false, CLIMBER)).isTrue();
        assertThat(MemberGovernance.canManageAccount(BOARD, false, SETTER)).isTrue();
        assertThat(MemberGovernance.canManageAccount(BOARD, false, BOARD)).isFalse();
        assertThat(MemberGovernance.canManageAccount(BOARD, false, ADMIN)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void theAdminManagesEveryoneElse(Role target) {
        assertThat(MemberGovernance.canManageAccount(ADMIN, false, target)).isTrue();
    }
}
