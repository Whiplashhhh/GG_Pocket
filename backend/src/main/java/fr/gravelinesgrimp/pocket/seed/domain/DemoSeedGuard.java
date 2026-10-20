package fr.gravelinesgrimp.pocket.seed.domain;

import fr.gravelinesgrimp.pocket.platform.domain.DeploymentEnvironment;

import java.util.Optional;

/**
 * Spec §17: safety catches of the demo seed, all required. The seeder bean itself only exists with
 * the {@code dev} Spring profile; on top of that it must be allowed explicitly, never run against a
 * production database, and never next to accounts it did not create.
 */
public final class DemoSeedGuard {

    private DemoSeedGuard() {}

    /**
     * @return why the seed must not run, or empty when it may
     */
    public static Optional<String> refusal(
            boolean allowed,
            DeploymentEnvironment configured,
            DeploymentEnvironment databaseMarker,
            long membersNotFromTheSeed,
            boolean passwordProvided) {
        if (!allowed) {
            return Optional.of("GG_ALLOW_DEMO_SEED is not true");
        }
        if (configured == DeploymentEnvironment.PROD
                || databaseMarker == DeploymentEnvironment.PROD) {
            return Optional.of("this is a production instance or database");
        }
        if (membersNotFromTheSeed > 0) {
            return Optional.of(
                    "the database holds "
                            + membersNotFromTheSeed
                            + " account(s) not created by it");
        }
        if (configured == DeploymentEnvironment.DEV && !passwordProvided) {
            return Optional.of("the deployed dev needs GG_SEED_PASSWORD (no weak default there)");
        }
        return Optional.empty();
    }
}
