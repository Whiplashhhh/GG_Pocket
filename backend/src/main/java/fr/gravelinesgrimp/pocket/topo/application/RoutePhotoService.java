package fr.gravelinesgrimp.pocket.topo.application;

import fr.gravelinesgrimp.pocket.member.application.ImageSanitizer;
import fr.gravelinesgrimp.pocket.member.application.MediaService;
import fr.gravelinesgrimp.pocket.member.domain.MediaUsage;
import fr.gravelinesgrimp.pocket.platform.application.RateLimiter;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.topo.domain.Route;
import fr.gravelinesgrimp.pocket.topo.infra.RouteRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * The photo of a route (setters and above, spec §5): the same cleaning as profile photos, but the
 * whole picture is kept, shrunk to fit in {@value #PHOTO_MAX_SIDE} px.
 */
@Service
public class RoutePhotoService {

    /** The app resizes to this before sending; the route sheet shows it full width. */
    static final int PHOTO_MAX_SIDE = 1600;

    private final RouteRepository routes;
    private final MediaService media;
    private final ImageSanitizer sanitizer;
    private final RateLimiter rateLimiter;
    private final RouteViews views;

    public RoutePhotoService(
            RouteRepository routes,
            MediaService media,
            ImageSanitizer sanitizer,
            RateLimiter rateLimiter,
            RouteViews views) {
        this.routes = routes;
        this.media = media;
        this.sanitizer = sanitizer;
        this.rateLimiter = rateLimiter;
        this.views = views;
    }

    /** The previous photo, if any, is deleted once the new one is committed. */
    @PreAuthorize("hasRole('SETTER')")
    @Transactional
    public RouteDetailView replace(UUID actorId, UUID routeId, byte[] upload) {
        var route = load(routeId);
        route.ensureOnTheWall();
        rateLimiter.consume(RateLimiter.ROUTE_PHOTO_UPLOAD_PER_MEMBER, actorId.toString());
        var image = sanitizer.fittedJpeg(upload, PHOTO_MAX_SIDE);
        var photoId = media.storeJpeg(actorId, MediaUsage.ROUTE_PHOTO, image);
        var previous = route.photoId();
        route.changePhoto(photoId);
        if (previous != null) {
            media.delete(previous);
        }
        return views.detail(route, actorId);
    }

    @PreAuthorize("hasRole('SETTER')")
    @Transactional
    public void remove(UUID routeId) {
        var route = load(routeId);
        var previous = route.photoId();
        route.changePhoto(null);
        if (previous != null) {
            media.delete(previous);
        }
    }

    private Route load(UUID routeId) {
        return routes.findById(routeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No route"));
    }
}
