package fr.gravelinesgrimp.pocket.seed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import fr.gravelinesgrimp.pocket.TestcontainersConfiguration;
import fr.gravelinesgrimp.pocket.auth.infra.RegistrationCodeRepository;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * The demo seed in the dev profile, on its own database (not {@code @IntegrationTest}: the seed
 * must start from an empty club, and its accounts must not leak into the other tests).
 */
@SpringBootTest(
        properties = {
            "spring.profiles.active=dev",
            "gg.demo-seed.allowed=true",
            "gg.media.root=target/test-media"
        })
@Import(TestcontainersConfiguration.class)
class DemoSeederIT {

    @Autowired private MemberRepository members;
    @Autowired private RegistrationCodeRepository codes;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private DemoSeeder seeder;

    @Test
    void createsOneAccountPerRoleAndADemoCode() {
        assertThat(members.findAll())
                .extracting(Member::nickname, Member::role)
                .containsExactlyInAnyOrder(
                        tuple("admin", Role.ADMIN),
                        tuple("direction", Role.BOARD),
                        tuple("ouvreur", Role.SETTER),
                        tuple("grimpeur", Role.CLIMBER));
        assertThat(members.findByNicknameIgnoreCase("grimpeur"))
                .get()
                .satisfies(
                        member ->
                                assertThat(
                                                passwordEncoder.matches(
                                                        DemoSeeder.LOCAL_PASSWORD,
                                                        member.passwordHash()))
                                        .isTrue());
        assertThat(codes.existsByCode(DemoSeeder.DEMO_CODE)).isTrue();
    }

    @Test
    void theFirstStartCodeStaysQuietAfterTheSeed() {
        assertThat(codes.findAllByGrantedRoleAndActiveTrue(Role.ADMIN)).isEmpty();
    }

    @Test
    void runningItAgainChangesNothing() {
        seeder.run(null);
        assertThat(members.count()).isEqualTo(4);
    }
}
