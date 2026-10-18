package fr.gravelinesgrimp.pocket;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Files of the repository that tests compare against. Tests run from the backend directory. */
public final class RepositoryFiles {

    public static final Path VERSION = Path.of("..", "VERSION");
    public static final Path OPENAPI_CONTRACT =
            Path.of("..", "frontend", "src", "api", "openapi.json");

    private RepositoryFiles() {}

    public static String version() {
        try {
            return Files.readString(VERSION).strip();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
