package fr.gravelinesgrimp.pocket.platform.application;

import fr.gravelinesgrimp.pocket.platform.domain.DeploymentEnvironment;

/** What is running: app version (from the VERSION file), environment, and git origin. */
public record AppMetadata(
        String version, DeploymentEnvironment environment, String commit, String branch) {}
