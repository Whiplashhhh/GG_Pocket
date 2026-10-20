package fr.gravelinesgrimp.pocket.auth.application;

import static org.assertj.core.api.Assertions.assertThat;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.auth.domain.RegistrationCode;
import fr.gravelinesgrimp.pocket.auth.infra.RegistrationCodeRepository;
import fr.gravelinesgrimp.pocket.member.domain.Role;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;

@IntegrationTest
class FirstStartAdminCodeIT {

    @Autowired private RegistrationCodeRepository codes;

    /** The test database starts empty, exactly like a brand new production. */
    @Test
    void theFirstStartLeftASingleUseAdminCode() {
        var adminCodes = codes.findAllByGrantedRoleOrderByCreatedAtDesc(Role.ADMIN);
        assertThat(adminCodes).isNotEmpty();
        RegistrationCode first = adminCodes.getLast();
        assertThat(first.maxUses()).isEqualTo(1);
        assertThat(first.expiresAt())
                .isBetween(
                        Instant.now().plus(Duration.ofHours(23)),
                        Instant.now().plus(Duration.ofHours(24)));
    }
}
