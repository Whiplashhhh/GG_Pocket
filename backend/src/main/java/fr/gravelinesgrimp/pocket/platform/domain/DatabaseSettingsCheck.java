package fr.gravelinesgrimp.pocket.platform.domain;

import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Function;

/**
 * Outside a developer's machine, the database settings must come from the environment: the local
 * defaults ({@code ggpocket} / {@code ggpocket}) are public, and a deployed instance falling back
 * on them would run on a database anyone can guess.
 */
public final class DatabaseSettingsCheck {

    /** Publicly known password of the local database (deploy/compose.local.yml). */
    static final String LOCAL_PASSWORD = "ggpocket";

    private DatabaseSettingsCheck() {}

    /**
     * @param environment the configured environment code ({@code local}, {@code dev}, {@code prod})
     * @param variables reads an environment variable, {@code null} when absent
     * @throws IllegalStateException when a deployed instance lacks its own database settings
     */
    public static void verify(String environment, Function<String, String> variables) {
        if (environment == null
                || DeploymentEnvironment.LOCAL
                        .code()
                        .equals(environment.toLowerCase(Locale.ROOT))) {
            return;
        }
        var missing = new ArrayList<String>();
        for (var name : new String[] {"GG_DB_URL", "GG_DB_USER", "GG_DB_PASSWORD"}) {
            var value = variables.apply(name);
            if (value == null || value.isBlank()) {
                missing.add(name);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "The "
                            + environment
                            + " environment needs its own database settings, missing: "
                            + String.join(", ", missing)
                            + ". Refusing to fall back on the local defaults.");
        }
        if (LOCAL_PASSWORD.equals(variables.apply("GG_DB_PASSWORD"))) {
            throw new IllegalStateException(
                    "The "
                            + environment
                            + " environment uses the public local database password. Generate a"
                            + " real one (see DEPLOYMENT.md).");
        }
    }
}
