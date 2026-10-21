package fr.gravelinesgrimp.pocket.member.application;

import fr.gravelinesgrimp.pocket.member.domain.MediaUsage;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.application.RateLimiter;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/** Profile photos: cleaned, stored on the media volume, one per member. */
@Service
public class AvatarService {

    /**
     * Side of the stored square; the app displays avatars at 40 to 96 px (×2 for sharp screens).
     */
    static final int AVATAR_SIZE = 512;

    private final MemberRepository members;
    private final MediaService media;
    private final ImageSanitizer sanitizer;
    private final RateLimiter rateLimiter;
    private final Clock clock;

    public AvatarService(
            MemberRepository members,
            MediaService media,
            ImageSanitizer sanitizer,
            RateLimiter rateLimiter,
            Clock clock) {
        this.members = members;
        this.media = media;
        this.sanitizer = sanitizer;
        this.rateLimiter = rateLimiter;
        this.clock = clock;
    }

    @Transactional
    public MyProfileView replace(UUID memberId, byte[] upload) {
        var member =
                members.findById(memberId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No member"));
        rateLimiter.consume(RateLimiter.AVATAR_UPLOAD_PER_MEMBER, memberId.toString());
        var image = sanitizer.squareJpeg(upload, AVATAR_SIZE);
        var avatarId = media.storeJpeg(memberId, MediaUsage.AVATAR, image);
        var previous = member.avatarId();
        member.changeAvatar(avatarId);
        if (previous != null) {
            media.delete(previous);
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
            media.delete(previous);
        }
    }
}
