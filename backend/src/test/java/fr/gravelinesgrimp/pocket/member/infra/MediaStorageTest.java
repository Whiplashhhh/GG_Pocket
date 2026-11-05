package fr.gravelinesgrimp.pocket.member.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import fr.gravelinesgrimp.pocket.platform.config.GgProperties;
import fr.gravelinesgrimp.pocket.platform.domain.DeploymentEnvironment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;

class MediaStorageTest {

    @TempDir Path root;

    private MediaStorage storage() {
        return new MediaStorage(
                new GgProperties(
                        DeploymentEnvironment.LOCAL,
                        new GgProperties.Git("unknown", "unknown"),
                        new GgProperties.Media(root)));
    }

    @Test
    void writesTheWholeFile() {
        storage().write("ab/photo.jpg", new byte[] {1, 2, 3});

        assertThat(storage().read("ab/photo.jpg")).containsExactly(1, 2, 3);
    }

    /** The backup container reads the photos through the API's group (D-112). */
    @Test
    void aStoredFileIsReadableByItsGroupAndNobodyElse() throws Exception {
        assumeTrue(Files.getFileAttributeView(root, PosixFileAttributeView.class) != null);

        storage().write("ab/photo.jpg", new byte[] {1});

        assertThat(
                        PosixFilePermissions.toString(
                                Files.getPosixFilePermissions(root.resolve("ab/photo.jpg"))))
                .isEqualTo("rw-r-----");
    }
}
