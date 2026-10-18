package fr.gravelinesgrimp.pocket.platform.application;

import static org.assertj.core.api.Assertions.assertThat;

import fr.gravelinesgrimp.pocket.RepositoryFiles;
import fr.gravelinesgrimp.pocket.platform.config.GgProperties;
import fr.gravelinesgrimp.pocket.platform.domain.DeploymentEnvironment;

import org.junit.jupiter.api.Test;

class AppMetadataServiceTest {

    @Test
    void versionComesFromTheRepositoryVersionFile() {
        assertThat(AppMetadataService.readVersion())
                .isEqualTo(RepositoryFiles.version())
                .matches("\\d+\\.\\d+\\.\\d+");
    }

    @Test
    void environmentAndGitOriginComeFromTheSettings() {
        var properties =
                new GgProperties(
                        DeploymentEnvironment.DEV, new GgProperties.Git("abc1234", "feature/x"));

        var metadata = new AppMetadataService(properties).current();

        assertThat(metadata)
                .isEqualTo(
                        new AppMetadata(
                                RepositoryFiles.version(),
                                DeploymentEnvironment.DEV,
                                "abc1234",
                                "feature/x"));
    }
}
