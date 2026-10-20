package fr.gravelinesgrimp.pocket.platform.config;

import fr.gravelinesgrimp.pocket.platform.domain.DeploymentEnvironment;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.nio.file.Path;

/**
 * Application settings under the {@code gg} prefix. Usually set through environment variables:
 * {@code GG_ENVIRONMENT}, {@code GG_GIT_COMMIT}, {@code GG_GIT_BRANCH}, {@code GG_MEDIA_ROOT}.
 *
 * @param environment where this instance runs; defaults to {@code local}
 * @param git commit and branch the running build comes from, injected by the Docker image
 * @param media where uploaded photos are stored
 */
@ConfigurationProperties(prefix = "gg")
public record GgProperties(
        @DefaultValue("local") DeploymentEnvironment environment,
        @DefaultValue Git git,
        @DefaultValue Media media) {

    public record Git(
            @DefaultValue("unknown") String commit, @DefaultValue("unknown") String branch) {}

    /**
     * @param root directory of the uploaded files (a Docker volume in dev and prod)
     */
    public record Media(@DefaultValue("data/media") Path root) {}
}
