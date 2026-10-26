package fr.gravelinesgrimp.pocket.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.gravelinesgrimp.pocket.auth.domain.RegistrationCode;
import fr.gravelinesgrimp.pocket.auth.infra.RegistrationCodeRepository;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

class FirstStartAdminCodeTest {

    private static final Instant NOW = Instant.parse("2026-10-20T10:00:00Z");

    private final MemberRepository members = mock(MemberRepository.class);
    private final RegistrationCodeRepository codes = mock(RegistrationCodeRepository.class);
    private final FirstStartAdminCode firstStart =
            new FirstStartAdminCode(members, codes, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void anEmptyClubGetsASingleUseAdminCodeValidOneDay() {
        when(members.countMembers()).thenReturn(0L);
        var previous =
                new RegistrationCode("AAAABBBB", "Premier démarrage", Role.ADMIN, NOW, 1, null);
        when(codes.findAllByGrantedRoleAndActiveTrue(Role.ADMIN)).thenReturn(List.of(previous));

        firstStart.run(null);

        var saved = ArgumentCaptor.forClass(RegistrationCode.class);
        verify(codes).save(saved.capture());
        assertThat(saved.getValue().grantedRole()).isEqualTo(Role.ADMIN);
        assertThat(saved.getValue().maxUses()).isEqualTo(1);
        assertThat(saved.getValue().expiresAt()).isEqualTo(NOW.plus(Duration.ofHours(24)));
        assertThat(saved.getValue().isUsable(NOW)).isTrue();
        assertThat(previous.active()).as("the code shown at the previous start").isFalse();
    }

    @Test
    void aClubWithMembersGetsNothing() {
        when(members.countMembers()).thenReturn(3L);

        firstStart.run(null);

        verify(codes, never()).save(any());
    }
}
