package fr.gravelinesgrimp.pocket.seed.application;

import static org.assertj.core.api.Assertions.assertThat;

import fr.gravelinesgrimp.pocket.TestcontainersConfiguration;
import fr.gravelinesgrimp.pocket.auth.infra.RegistrationCodeRepository;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * The demo seed of the deployed dev environment: accounts with the password from the environment,
 * and no registration code, the dev staying closed to strangers (spec §15.4).
 */
@SpringBootTest(
        properties = {
            "spring.profiles.active=dev",
            "gg.environment=dev",
            "gg.demo-seed.allowed=true",
            "gg.demo-seed.password=a-strong-dev-password",
            "gg.media.root=target/test-media",
            // Read by DatabaseSettingsCheck; the datasource itself comes from Testcontainers.
            "GG_DB_URL=jdbc:postgresql://testcontainers/ggpocket",
            "GG_DB_USER=test",
            "GG_DB_PASSWORD=not-the-local-one"
        })
@Import(TestcontainersConfiguration.class)
class DemoSeederDeployedDevIT {

    @Autowired private MemberRepository members;
    @Autowired private RegistrationCodeRepository codes;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void createsTheAccountsWithTheirPasswordButNoRegistrationCode() {
        assertThat(members.countMembers()).isEqualTo(4);
        assertThat(members.findByNicknameIgnoreCase("grimpeur"))
                .get()
                .satisfies(
                        member ->
                                assertThat(
                                                passwordEncoder.matches(
                                                        "a-strong-dev-password",
                                                        member.passwordHash()))
                                        .isTrue());
        assertThat(codes.existsByCode(DemoSeeder.DEMO_CODE)).isFalse();
    }
}
