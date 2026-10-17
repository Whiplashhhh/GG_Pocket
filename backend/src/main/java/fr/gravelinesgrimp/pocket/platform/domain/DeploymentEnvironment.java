package fr.gravelinesgrimp.pocket.platform.domain;

import java.util.Locale;

/**
 * Where this instance runs. Drives safety guards (demo seed, database marker) and the dev banner.
 */
public enum DeploymentEnvironment {
    LOCAL,
    DEV,
    PROD;

    /** Lower-case identifier exposed by the API ({@code local}, {@code dev}, {@code prod}). */
    public String code() {
        return name().toLowerCase(Locale.ROOT);
    }
}
