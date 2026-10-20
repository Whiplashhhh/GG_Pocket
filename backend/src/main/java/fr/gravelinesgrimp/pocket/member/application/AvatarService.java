package fr.gravelinesgrimp.pocket.member.application;

import fr.gravelinesgrimp.pocket.member.domain.MediaFile;
import fr.gravelinesgrimp.pocket.member.domain.MediaUsage;
import fr.gravelinesgrimp.pocket.member.infra.MediaFileRepository;
import fr.gravelinesgrimp.pocket.member.infra.MediaStorage;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.util.UUID;

/** Profile photos: cleaned, stored on the media volume, one per member. */
@Service
public class AvatarService {

    /**
     * Side of the stored square; the app displays avatars at 40 to 96 px (×2 for sharp screens).
     */
    static final int AVATAR_SIZE = 512;

    private static final String CONTENT_TYPE = "image/jpeg";

    private final MemberRepository members;
    private final MediaFileRepository mediaFiles;
    private final MediaStorage storage;
    private final ImageSanitizer sanitizer;
    private final Clock clock;

    public AvatarService(
            MemberRepository members,
            MediaFileRepository mediaFiles,
            MediaStorage storage,
            ImageSanitizer sanitizer,
            Clock clock) {
        this.members = members;
        this.mediaFiles = mediaFiles;
        this.storage = storage;
        this.sanitizer = sanitizer;
        this.clock = clock;
    }

    @Transactional
    public MyProfileView replace(UUID memberId, byte[] upload) {
        var member =
                members.findById(memberId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No member"));
        var image = sanitizer.squareJpeg(upload, AVATAR_SIZE);
        var media =
                new MediaFile(
                        memberId,
                        MediaUsage.AVATAR,
                        CONTENT_TYPE,
                        image.width(),
                        image.height(),
                        image.jpeg().length,
                        "jpg");
        storage.write(media.storageKey(), image.jpeg());
        deleteFileIfRolledBack(media.storageKey());
        mediaFiles.save(media);
        var previous = member.avatarId();
        member.changeAvatar(media.id());
        if (previous != null) {
            removeMedia(previous);
        }
        return MyProfileView.of(member, ClubTime.today(clock));
    }

    @Transactional
    public void remove(UUID memberId) {
        var member =
                members.findById(memberId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No member"));
        var previous = member.avatarId();
        if (previous != null) {
            member.changeAvatar(null);
            removeMedia(previous);
        }
    }

    /** Deletes the row now and the file once the transaction has committed. */
    private void removeMedia(UUID mediaId) {
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
