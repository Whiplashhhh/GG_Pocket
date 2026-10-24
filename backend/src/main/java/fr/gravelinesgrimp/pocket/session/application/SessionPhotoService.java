package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.member.application.ImageSanitizer;
import fr.gravelinesgrimp.pocket.member.application.MediaService;
import fr.gravelinesgrimp.pocket.member.domain.MediaUsage;
import fr.gravelinesgrimp.pocket.platform.application.RateLimiter;
import fr.gravelinesgrimp.pocket.session.domain.SessionCalendar;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * The photo of a session (spec §8.3, §14): cleaned like a route photo (D-079), shrunk to fit in
 * {@value #PHOTO_MAX_SIDE} px, seen only by those who may read the session.
 */
@Service
public class SessionPhotoService {

    /** The app resizes to this before sending. */
    static final int PHOTO_MAX_SIDE = 1600;

    private final OwnedSessions sessions;
    private final SessionViews views;
    private final MediaService media;
    private final ImageSanitizer sanitizer;
    private final RateLimiter rateLimiter;
    private final Clock clock;

    public SessionPhotoService(
            OwnedSessions sessions,
            SessionViews views,
            MediaService media,
            ImageSanitizer sanitizer,
            RateLimiter rateLimiter,
            Clock clock) {
        this.sessions = sessions;
        this.views = views;
        this.media = media;
        this.sanitizer = sanitizer;
        this.rateLimiter = rateLimiter;
        this.clock = clock;
    }

    /**
     * The previous photo, if any, is deleted once the new one is committed.
     *
     * @throws fr.gravelinesgrimp.pocket.platform.domain.BusinessException {@code NOT_FOUND}, {@code
     *     MONTH_FROZEN}, {@code IMAGE_UNSUPPORTED}, {@code IMAGE_TOO_LARGE}, {@code RATE_LIMITED}
     */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional
    public SessionView replace(UUID memberId, UUID sessionId, byte[] upload) {
        var session = sessions.load(memberId, sessionId);
        SessionCalendar.checkNotFrozen(session.climbedOn(), clock.instant());
        rateLimiter.consume(RateLimiter.SESSION_PHOTO_UPLOAD_PER_MEMBER, memberId.toString());
        var image = sanitizer.fittedJpeg(upload, PHOTO_MAX_SIDE);
        var photoId = media.storeJpeg(memberId, MediaUsage.SESSION_PHOTO, image);
        var previous = session.photoId();
        session.changePhoto(photoId);
        if (previous != null) {
            media.delete(previous);
        }
        return views.of(session, memberId);
    }

    /** Idempotent. */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional
    public void remove(UUID memberId, UUID sessionId) {
        var session = sessions.load(memberId, sessionId);
        SessionCalendar.checkNotFrozen(session.climbedOn(), clock.instant());
        var previous = session.photoId();
        session.changePhoto(null);
        if (previous != null) {
            media.delete(previous);
        }
    }
}
