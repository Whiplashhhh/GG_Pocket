package fr.gravelinesgrimp.pocket.member.application;

import fr.gravelinesgrimp.pocket.member.domain.MediaUsage;

import java.util.UUID;

/**
 * Says who may see the files of one usage that is not open to every member (session photos…).
 * Implemented by the module that owns those files, so that {@link MediaService} does not depend on
 * it.
 */
public interface PrivateMediaAccess {

    MediaUsage usage();

    /** Called inside a read-only transaction. */
    boolean canRead(UUID readerId, UUID mediaId);
}
