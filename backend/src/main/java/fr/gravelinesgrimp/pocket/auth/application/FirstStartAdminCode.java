package fr.gravelinesgrimp.pocket.auth.application;

import fr.gravelinesgrimp.pocket.auth.domain.RegistrationCode;
import fr.gravelinesgrimp.pocket.auth.domain.RegistrationCodeFormat;
import fr.gravelinesgrimp.pocket.auth.infra.RegistrationCodeRepository;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;

/**
 * Spec §16: a database without any member gets a single-use admin registration code, valid 24
 * hours, printed once in the logs. There is no default password anywhere. Each start-up while
 * nobody has registered yet replaces the previous code (it was shown once; a new one is shown).
 */
@Component
public class FirstStartAdminCode implements ApplicationRunner {

    static final Duration VALIDITY = Duration.ofHours(24);
    static final String LABEL = "Premier démarrage";

    private static final Logger log = LoggerFactory.getLogger(FirstStartAdminCode.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final MemberRepository members;
    private final RegistrationCodeRepository codes;
    private final Clock clock;

    public FirstStartAdminCode(
            MemberRepository members, RegistrationCodeRepository codes, Clock clock) {
        this.members = members;
        this.codes = codes;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (members.countMembers() > 0) {
            return;
        }
        codes.findAllByGrantedRoleAndActiveTrue(Role.ADMIN).forEach(RegistrationCode::deactivate);
        String code;
        do {
            code = RegistrationCodeFormat.generate(RANDOM);
        } while (codes.existsByCode(code));
        codes.save(
                new RegistrationCode(
                        code, LABEL, Role.ADMIN, clock.instant().plus(VALIDITY), 1, null));
        log.warn(
                "No member yet. Single-use admin registration code, valid 24 hours: {}"
                        + " (shown only this once; a restart before anyone registers issues a new"
                        + " one)",
                RegistrationCodeFormat.display(code));
    }
}
