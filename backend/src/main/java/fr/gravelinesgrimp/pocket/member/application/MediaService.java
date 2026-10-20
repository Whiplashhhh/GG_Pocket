package fr.gravelinesgrimp.pocket.member.application;

import fr.gravelinesgrimp.pocket.member.domain.MediaUsage;
import fr.gravelinesgrimp.pocket.member.infra.MediaFileRepository;
import fr.gravelinesgrimp.pocket.member.infra.MediaStorage;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Serves uploaded files after checking that the reader may see them. */
@Service
public class MediaService {

    /** Bytes and type of a file the caller is allowed to see. */
    public record MediaContent(byte[] bytes, String contentType) {}

    private final MediaFileRepository mediaFiles;
    private final MediaStorage storage;

    public MediaService(MediaFileRepository mediaFiles, MediaStorage storage) {
        this.mediaFiles = mediaFiles;
        this.storage = storage;
    }

    /**
     * Avatars are visible to every signed-in member (the caller of this method is one, checked by
     * the security chain). Later usages (session photos) will check friendship here.
     */
    @Transactional(readOnly = true)
    public MediaContent load(UUID mediaId) {
        var media =
                mediaFiles
                        .findById(mediaId)
                        .filter(file -> file.usage() == MediaUsage.AVATAR)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No media"));
        return new MediaContent(storage.read(media.storageKey()), media.contentType());
    }
}
