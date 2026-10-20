package fr.gravelinesgrimp.pocket.platform.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import java.util.Map;

class DatabaseSettingsCheckTest {

    private static final Map<String, String> COMPLETE =
            Map.of(
                    "GG_DB_URL", "jdbc:postgresql://db:5432/ggpocket",
                    "GG_DB_USER", "ggpocket",
                    "GG_DB_PASSWORD", "n0t-the-local-one");

    @Test
    void aLocalMachineMayUseTheDefaults() {
        assertThatCode(() -> DatabaseSettingsCheck.verify("local", Map.<String, String>of()::get))
                .doesNotThrowAnyException();
    }

    @Test
    void aDeployedInstanceNeedsAllThreeVariables() {
        var withoutPassword = Map.of("GG_DB_URL", "jdbc:postgresql://db/x", "GG_DB_USER", "u");

        assertThatThrownBy(() -> DatabaseSettingsCheck.verify("dev", withoutPassword::get))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GG_DB_PASSWORD")
                .hasMessageNotContaining("GG_DB_USER");
        assertThatThrownBy(
                        () -> DatabaseSettingsCheck.verify("prod", Map.of("GG_DB_URL", " ")::get))
                .hasMessageContaining("GG_DB_URL, GG_DB_USER, GG_DB_PASSWORD");
    }

    @Test
    void aDeployedInstanceRefusesThePublicLocalPassword() {
        var local = Map.of("GG_DB_URL", "jdbc:x", "GG_DB_USER", "u", "GG_DB_PASSWORD", "ggpocket");

        assertThatThrownBy(() -> DatabaseSettingsCheck.verify("DEV", local::get))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("public local database password");
    }

    @Test
    void aDeployedInstanceWithItsOwnSettingsStarts() {
        assertThatCode(() -> DatabaseSettingsCheck.verify("prod", COMPLETE::get))
                .doesNotThrowAnyException();
    }
}
