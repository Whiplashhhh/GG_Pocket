package fr.gravelinesgrimp.pocket.member.application;

import fr.gravelinesgrimp.pocket.member.application.ImageSanitizer.CleanImage;
import fr.gravelinesgrimp.pocket.member.domain.MediaFile;
import fr.gravelinesgrimp.pocket.member.domain.MediaUsage;
import fr.gravelinesgrimp.pocket.member.infra.MediaFileRepository;
import fr.gravelinesgrimp.pocket.member.infra.MediaStorage;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Stores cleaned files on the media volume, and serves them after checking that the reader may see
 * them.
 */
@Service
public class MediaService {

    /** Bytes and type of a file the caller is allowed to see. */
    public record MediaContent(byte[] bytes, String contentType) {}

    private static final String JPEG = "image/jpeg";

    private final MediaFileRepository mediaFiles;
    private final MediaStorage storage;
    private final Map<MediaUsage, PrivateMediaAccess> privateAccess;

    public MediaService(
            MediaFileRepository mediaFiles,
            MediaStorage storage,
            List<PrivateMediaAccess> privateAccess) {
        this.mediaFiles = mediaFiles;
        this.storage = storage;
        this.privateAccess =
                privateAccess.stream()
                        .collect(Collectors.toMap(PrivateMediaAccess::usage, Function.identity()));
    }

    /**
     * Avatars and route photos are visible to every signed-in member (the caller of this method is
     * one, checked by the security chain). Other files are visible only when the module that owns
     * them says so ({@link PrivateMediaAccess}); a usage nobody vouches for is never served. A file
     * the reader may not see is "not found", like one that does not exist.
     */
    @Transactional(readOnly = true)
    public MediaContent load(UUID readerId, UUID mediaId) {
        var media =
                mediaFiles
                        .findById(mediaId)
                        .filter(file -> visible(file, readerId))
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No media"));
        return new MediaContent(storage.read(media.storageKey()), media.contentType());
    }

    private boolean visible(MediaFile file, UUID readerId) {
        if (file.usage().visibleToEveryMember()) {
            return true;
        }
        var access = privateAccess.get(file.usage());
        return access != null && access.canRead(readerId, file.id());
    }

    /**
     * Writes a cleaned JPEG and its row, within the caller's transaction: if it rolls back, the
     * file is removed from the volume too. Returns the id of the new file.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public UUID storeJpeg(UUID ownerId, MediaUsage usage, CleanImage image) {
        var media =
                new MediaFile(
                        ownerId,
                        usage,
                        JPEG,
                        image.width(),
                        image.height(),
                        image.jpeg().length,
                        "jpg");
        storage.write(media.storageKey(), image.jpeg());
        deleteFileIfRolledBack(media.storageKey());
        mediaFiles.save(media);
        return media.id();
    }

    /** Deletes the row now and the file once the transaction has committed. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void delete(UUID mediaId) {
        mediaFiles
                .findById(mediaId)
                .ifPresent(
                        media -> {
                            mediaFiles.delete(media);
                            var key = media.storageKey();
                            TransactionSynchronizationManager.registerSynchronization(
                                    new TransactionSynchronization() {
                                        @Override
                                        public void afterCommit() {
                                            storage.delete(key);
                                        }
                                    });
                        });
    }

    /** A file written for a transaction that fails must not stay on the volume. */
    private void deleteFileIfRolledBack(String key) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status != STATUS_COMMITTED) {
                            storage.delete(key);
                        }
                    }
                });
    }
}
