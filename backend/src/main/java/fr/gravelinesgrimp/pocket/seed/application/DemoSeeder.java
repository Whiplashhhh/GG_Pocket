package fr.gravelinesgrimp.pocket.seed.application;

import fr.gravelinesgrimp.pocket.auth.domain.RegistrationCode;
import fr.gravelinesgrimp.pocket.auth.infra.RegistrationCodeRepository;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.MemberIdentity;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.config.GgProperties;
import fr.gravelinesgrimp.pocket.platform.domain.DeploymentEnvironment;
import fr.gravelinesgrimp.pocket.platform.domain.InstanceMarker;
import fr.gravelinesgrimp.pocket.platform.infra.InstanceMarkerRepository;
import fr.gravelinesgrimp.pocket.seed.domain.DemoSeedGuard;
import fr.gravelinesgrimp.pocket.seed.infra.DemoSeedRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Demo accounts for the local machine and the dev environment (spec §17): one per role, plus a
 * registration code. Idempotent: accounts already there are left alone. The rest of the demo data
 * (routes, sessions, friends…) is added phase after phase.
 *
 * <p>Runs before the first-start admin code, which then sees members and stays quiet.
 */
@Component
@Profile("dev")
@Order(0)
public class DemoSeeder implements ApplicationRunner {

    /** Local machine only: the deployed dev must provide GG_SEED_PASSWORD. */
    static final String LOCAL_PASSWORD = "grimpe-en-local";

    static final String DEMO_CODE = "DEMO2026";

    record DemoAccount(String nickname, String firstName, String lastName, Role role) {}

    /** Fictional people (spec §17: no real person). */
    static final List<DemoAccount> ACCOUNTS =
            List.of(
                    new DemoAccount("admin", "Camille", "Roux", Role.ADMIN),
                    new DemoAccount("direction", "Sacha", "Lefebvre", Role.BOARD),
                    new DemoAccount("ouvreur", "Noa", "Martin", Role.SETTER),
                    new DemoAccount("grimpeur", "Lou", "Bernard", Role.CLIMBER));

    private static final Logger log = LoggerFactory.getLogger(DemoSeeder.class);

    private final MemberRepository members;
    private final RegistrationCodeRepository codes;
    private final DemoSeedRegistry registry;
    private final InstanceMarkerRepository markers;
    private final PasswordEncoder passwordEncoder;
    private final GgProperties properties;
    private final Clock clock;
    private final boolean allowed;
    private final String password;

    public DemoSeeder(
            MemberRepository members,
            RegistrationCodeRepository codes,
            DemoSeedRegistry registry,
            InstanceMarkerRepository markers,
            PasswordEncoder passwordEncoder,
            GgProperties properties,
            Clock clock,
            @Value("${gg.demo-seed.allowed:false}") boolean allowed,
            @Value("${gg.demo-seed.password:}") String password) {
        this.members = members;
        this.codes = codes;
        this.registry = registry;
        this.markers = markers;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.clock = clock;
        this.allowed = allowed;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        var marker =
                markers.findById(InstanceMarker.SINGLETON_ID)
                        .map(InstanceMarker::environment)
                        .orElse(properties.environment());
        var refusal =
                DemoSeedGuard.refusal(
                        allowed,
                        properties.environment(),
                        marker,
                        registry.countMembersNotFromTheSeed(),
                        !password.isBlank());
        if (refusal.isPresent()) {
            log.warn("Demo seed skipped: {}", refusal.get());
            return;
        }
        var effectivePassword =
                properties.environment() == DeploymentEnvironment.LOCAL && password.isBlank()
                        ? LOCAL_PASSWORD
                        : password;
        var now = clock.instant();
        var hash = passwordEncoder.encode(effectivePassword);
        var created = 0;
        for (var account : ACCOUNTS) {
            if (members.findByNicknameIgnoreCase(account.nickname()).isPresent()) {
                continue;
            }
            var member =
                    members.save(
                            new Member(
                                    new MemberIdentity(
                                            account.nickname(),
                                            account.firstName(),
                                            account.lastName(),
                                            null),
                                    hash,
                                    account.role(),
                                    LocalDate.of(1992, 6, 15),
                                    false,
                                    now));
            members.flush();
            registry.register(member.id(), now);
            created++;
        }
        if (!codes.existsByCode(DEMO_CODE)) {
            codes.save(
                    new RegistrationCode(
                            DEMO_CODE, "Code de démonstration", Role.CLIMBER, null, null, null));
        }
        log.info(
                "Demo seed: {} account(s) created (admin, direction, ouvreur, grimpeur),"
                        + " registration code DEMO-2026",
                created);
    }
}
