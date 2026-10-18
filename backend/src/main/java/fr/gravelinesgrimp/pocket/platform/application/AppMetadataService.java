package fr.gravelinesgrimp.pocket.platform.application;

import fr.gravelinesgrimp.pocket.platform.config.GgProperties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@Service
public class AppMetadataService {

    /** Copied from the repository's VERSION file at build time (see pom.xml). */
    static final String VERSION_RESOURCE = "META-INF/gg-pocket/VERSION";

    private final AppMetadata metadata;

    public AppMetadataService(GgProperties properties) {
        this.metadata =
                new AppMetadata(
                        readVersion(),
                        properties.environment(),
                        properties.git().commit(),
                        properties.git().branch());
    }

    public AppMetadata current() {
        return metadata;
    }

    static String readVersion() {
        var resource = new ClassPathResource(VERSION_RESOURCE);
        if (!resource.exists()) {
            throw new IllegalStateException(
                    VERSION_RESOURCE + " is missing from the classpath: build with Maven.");
        }
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8).strip();
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + VERSION_RESOURCE, e);
        }
    }
}
