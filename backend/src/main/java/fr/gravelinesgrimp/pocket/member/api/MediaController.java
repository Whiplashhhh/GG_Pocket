package fr.gravelinesgrimp.pocket.member.api;

import fr.gravelinesgrimp.pocket.member.application.MediaService;
import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
@Tag(name = "Media")
class MediaController {

    /**
     * A file never changes: a new photo gets a new id. Private: only this browser may cache it.
     * Avatars and route photos, shown to every member, stay a year.
     */
    private static final CacheControl CACHE =
            CacheControl.maxAge(Duration.ofDays(365)).cachePrivate().immutable();

    /**
     * A session photo follows the session's audience, which may shrink (juste moi, a friendship
     * ended): a day at most in the browser of someone who may no longer see it (D-112).
     */
    private static final CacheControl PRIVATE_CACHE =
            CacheControl.maxAge(Duration.ofDays(1)).cachePrivate();

    private final MediaService media;

    MediaController(MediaService media) {
        this.media = media;
    }

    @GetMapping(path = "/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    @Operation(operationId = "getMedia", summary = "An uploaded image, after an access check")
    ResponseEntity<byte[]> get(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id) {
        var content = media.load(me.memberId(), id);
        return ResponseEntity.ok()
                .cacheControl(content.everyMember() ? CACHE : PRIVATE_CACHE)
                .contentType(MediaType.parseMediaType(content.contentType()))
                .body(content.bytes());
    }
}
