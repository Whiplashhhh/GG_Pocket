package fr.gravelinesgrimp.pocket.member.domain;

import static fr.gravelinesgrimp.pocket.member.domain.Role.ADMIN;
import static fr.gravelinesgrimp.pocket.member.domain.Role.BOARD;
import static fr.gravelinesgrimp.pocket.member.domain.Role.CLIMBER;
import static fr.gravelinesgrimp.pocket.member.domain.Role.SETTER;
import static fr.gravelinesgrimp.pocket.member.domain.Role.TOURIST;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** The role matrix of spec §5, case by case. */
class MemberGovernanceTest {

    @Test
    void theBoardSwitchesTouristsClimbersAndSettersOnly() {
        assertThat(MemberGovernance.assignableRoles(BOARD, false, TOURIST))
                .containsExactlyInAnyOrder(CLIMBER, SETTER);
        assertThat(MemberGovernance.assignableRoles(BOARD, false, CLIMBER))
                .containsExactlyInAnyOrder(TOURIST, SETTER);
        assertThat(MemberGovernance.assignableRoles(BOARD, false, SETTER))
                .containsExactlyInAnyOrder(TOURIST, CLIMBER);
        assertThat(MemberGovernance.assignableRoles(BOARD, false, BOARD)).isEmpty();
        assertThat(MemberGovernance.assignableRoles(BOARD, false, ADMIN)).isEmpty();
    }

    @Test
    void theAdminGivesAnyRole() {
        assertThat(MemberGovernance.assignableRoles(ADMIN, false, CLIMBER))
                .containsExactlyInAnyOrder(TOURIST, SETTER, BOARD, ADMIN);
        assertThat(MemberGovernance.assignableRoles(ADMIN, false, ADMIN))
                .containsExactlyInAnyOrder(TOURIST, CLIMBER, SETTER, BOARD);
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void nobodyChangesTheirOwnRoleNorManagesTheirOwnAccount(Role role) {
        assertThat(MemberGovernance.assignableRoles(role, true, role)).isEmpty();
        assertThat(MemberGovernance.canManageAccount(role, true, role)).isFalse();
        assertThat(MemberGovernance.canDelete(role, true, false)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(
            value = Role.class,
            names = {"TOURIST", "CLIMBER", "SETTER"})
    void touristsClimbersAndSettersManageNobody(Role actor) {
        for (var target : Role.values()) {
            assertThat(MemberGovernance.assignableRoles(actor, false, target)).isEmpty();
            assertThat(MemberGovernance.canManageAccount(actor, false, target)).isFalse();
        }
        assertThat(MemberGovernance.canDelete(actor, false, false)).isFalse();
    }

    @Test
    void theBoardOnlyDeletesAccountsItNeverValidated() {
        assertThat(MemberGovernance.canDelete(BOARD, false, false)).isTrue();
        assertThat(MemberGovernance.canDelete(BOARD, false, true)).isFalse();
        assertThat(MemberGovernance.canDelete(ADMIN, false, true)).isTrue();
        assertThat(MemberGovernance.canDelete(ADMIN, false, false)).isTrue();
    }

    @Test
    void theBoardManagesTheAccountsOfTouristsClimbersAndSettersOnly() {
        assertThat(MemberGovernance.canManageAccount(BOARD, false, TOURIST)).isTrue();
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
