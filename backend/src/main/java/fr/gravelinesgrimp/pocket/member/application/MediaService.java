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

import java.util.UUID;

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

    public MediaService(MediaFileRepository mediaFiles, MediaStorage storage) {
        this.mediaFiles = mediaFiles;
        this.storage = storage;
    }

    /**
     * Avatars and route photos are visible to every signed-in member (the caller of this method is
     * one, checked by the security chain). Later usages (session photos) will check friendship
     * here.
     */
    @Transactional(readOnly = true)
    public MediaContent load(UUID mediaId) {
        var media =
                mediaFiles
                        .findById(mediaId)
                        .filter(file -> file.usage().visibleToEveryMember())
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No media"));
        return new MediaContent(storage.read(media.storageKey()), media.contentType());
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
