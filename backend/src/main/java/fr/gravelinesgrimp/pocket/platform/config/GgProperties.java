package fr.gravelinesgrimp.pocket.platform.config;

import fr.gravelinesgrimp.pocket.platform.domain.DeploymentEnvironment;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Application settings under the {@code gg} prefix. Usually set through environment variables:
 * {@code GG_ENVIRONMENT}, {@code GG_GIT_COMMIT}, {@code GG_GIT_BRANCH}.
 *
 * @param environment where this instance runs; defaults to {@code local}
 * @param git commit and branch the running build comes from, injected by the Docker image
 */
@ConfigurationProperties(prefix = "gg")
public record GgProperties(
        @DefaultValue("local") DeploymentEnvironment environment, @DefaultValue Git git) {

    public record Git(
            @DefaultValue("unknown") String commit, @DefaultValue("unknown") String branch) {}
}
