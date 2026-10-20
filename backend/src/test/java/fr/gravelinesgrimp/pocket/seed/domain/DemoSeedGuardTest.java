package fr.gravelinesgrimp.pocket.seed.domain;

import static fr.gravelinesgrimp.pocket.platform.domain.DeploymentEnvironment.DEV;
import static fr.gravelinesgrimp.pocket.platform.domain.DeploymentEnvironment.LOCAL;
import static fr.gravelinesgrimp.pocket.platform.domain.DeploymentEnvironment.PROD;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Spec §17: every safety catch of the demo seed, one by one. */
class DemoSeedGuardTest {

    @Test
    void runsLocallyWhenAllowedOnAnEmptyDatabase() {
        assertThat(DemoSeedGuard.refusal(true, LOCAL, LOCAL, 0, false)).isEmpty();
    }

    @Test
    void needsToBeAllowedExplicitly() {
        assertThat(DemoSeedGuard.refusal(false, LOCAL, LOCAL, 0, false)).isPresent();
    }

    @Test
    void neverTouchesProduction() {
        assertThat(DemoSeedGuard.refusal(true, PROD, PROD, 0, true)).isPresent();
        // A dev instance plugged into a production database, by mistake.
        assertThat(DemoSeedGuard.refusal(true, DEV, PROD, 0, true)).isPresent();
    }

    @Test
    void neverRunsNextToRealAccounts() {
        assertThat(DemoSeedGuard.refusal(true, LOCAL, LOCAL, 1, false)).isPresent();
    }

    @Test
    void theDeployedDevNeedsItsOwnPassword() {
        assertThat(DemoSeedGuard.refusal(true, DEV, DEV, 0, false)).isPresent();
        assertThat(DemoSeedGuard.refusal(true, DEV, DEV, 0, true)).isEmpty();
    }
}
