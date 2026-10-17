package fr.gravelinesgrimp.pocket.platform.domain;

/** The database belongs to another environment than the one this application is configured for. */
public class EnvironmentMismatchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EnvironmentMismatchException(
            DeploymentEnvironment configured, DeploymentEnvironment databaseMarker) {
        super(
                "This database is marked as belonging to the "
                        + databaseMarker
                        + " environment, but the application is configured as "
                        + configured
                        + " (GG_ENVIRONMENT). Refusing to start to protect the data. Point the"
                        + " application to the right database or fix GG_ENVIRONMENT.");
    }
}
