package fr.gravelinesgrimp.pocket.member.infra;

import fr.gravelinesgrimp.pocket.platform.config.GgProperties;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Stores uploaded files on the media volume ({@code gg.media.root}). Keys come from {@code
 * MediaFile}, never from the client, and are checked to stay inside the root anyway.
 */
@Component
public class MediaStorage {

    private final Path root;

    public MediaStorage(GgProperties properties) {
        this.root = properties.media().root().toAbsolutePath().normalize();
    }

    /** Writes the whole file or nothing (temporary file, then atomic move). */
    public void write(String key, byte[] content) {
        var target = resolve(key);
        try {
            Files.createDirectories(target.getParent());
            var temporary = Files.createTempFile(target.getParent(), ".upload-", ".tmp");
            try {
                Files.write(temporary, content);
                Files.move(
                        temporary,
                        target,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temporary);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store media file " + key, e);
        }
    }

    public byte[] read(String key) {
        try {
            return Files.readAllBytes(resolve(key));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read media file " + key, e);
        }
    }

    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete media file " + key, e);
        }
    }

    private Path resolve(String key) {
        var path = root.resolve(key).normalize();
        if (!path.startsWith(root)) {
            throw new IllegalArgumentException("Media key escapes the media root: " + key);
        }
        return path;
    }
}
