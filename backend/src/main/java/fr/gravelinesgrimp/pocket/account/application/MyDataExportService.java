package fr.gravelinesgrimp.pocket.account.application;

import fr.gravelinesgrimp.pocket.auth.application.DeviceService;
import fr.gravelinesgrimp.pocket.member.domain.MediaUsage;
import fr.gravelinesgrimp.pocket.member.infra.MediaFileRepository;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;
import fr.gravelinesgrimp.pocket.session.infra.ClimbingSessionRepository;
import fr.gravelinesgrimp.pocket.topo.domain.Route;
import fr.gravelinesgrimp.pocket.topo.infra.FavoriteRepository;
import fr.gravelinesgrimp.pocket.topo.infra.LastChanceAckRepository;
import fr.gravelinesgrimp.pocket.topo.infra.RouteCheckRepository;
import fr.gravelinesgrimp.pocket.topo.infra.RouteCommentRepository;
import fr.gravelinesgrimp.pocket.topo.infra.RouteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * "Exporter mes données" (spec §14): everything the app keeps about me, in one JSON file. Routes
 * are named so the file reads without the app; photos are listed with their address in the app.
 */
@Service
public class MyDataExportService {

    public record MyDataExport(
            Instant exportedAt,
            ExportedProfile profile,
            List<ExportedSession> sessions,
            List<ExportedTick> routeChecks,
            List<ExportedTick> favorites,
            List<ExportedComment> routeComments,
            List<ExportedTick> lastChanceAcknowledgements,
            List<ExportedRoute> routesSet,
            List<ExportedDevice> devices,
            List<ExportedPhoto> photos) {}

    public record ExportedProfile(
            String nickname,
            String firstName,
            String lastName,
            String email,
            String role,
            String status,
            LocalDate birthDate,
            boolean showAge,
            Integer clubArrivalYear,
            Integer climbingSinceYear,
            String bio,
            boolean appearsInRankings,
            ExportedRoute favoriteRoute,
            Instant privacyAcceptedAt,
            Instant parentalConsentAt,
            Instant registeredAt,
            Instant lastSeenAt) {}

    public record ExportedSession(
            LocalDate climbedOn,
            Integer feeling,
            String comment,
            String visibility,
            UUID photoId,
            Instant createdAt,
            List<ExportedAscent> ascents) {}

    public record ExportedAscent(
            ExportedRoute route,
            String mode,
            boolean topped,
            boolean clean,
            int falls,
            boolean firstTry,
            Integer feeling) {}

    public record ExportedRoute(UUID id, String name) {}

    public record ExportedTick(ExportedRoute route, String mode, String source, Instant at) {}

    public record ExportedComment(
            ExportedRoute route, String body, Instant at, Instant deletedAt) {}

    public record ExportedDevice(
            String browser, String platform, Instant signedInAt, Instant lastActiveAt) {}

    public record ExportedPhoto(UUID id, String usage, int width, int height, Instant at) {}

    private final MemberRepository members;
    private final ClimbingSessionRepository sessions;
    private final RouteRepository routes;
    private final RouteCheckRepository checks;
    private final FavoriteRepository favorites;
    private final RouteCommentRepository comments;
    private final LastChanceAckRepository lastChanceAcks;
    private final MediaFileRepository mediaFiles;
    private final DeviceService devices;
    private final Clock clock;

    public MyDataExportService(
            MemberRepository members,
            ClimbingSessionRepository sessions,
            RouteRepository routes,
            RouteCheckRepository checks,
            FavoriteRepository favorites,
            RouteCommentRepository comments,
            LastChanceAckRepository lastChanceAcks,
            MediaFileRepository mediaFiles,
            DeviceService devices,
            Clock clock) {
        this.members = members;
        this.sessions = sessions;
        this.routes = routes;
        this.checks = checks;
        this.favorites = favorites;
        this.comments = comments;
        this.lastChanceAcks = lastChanceAcks;
        this.mediaFiles = mediaFiles;
        this.devices = devices;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public MyDataExport export(UUID memberId) {
        var member =
                members.findById(memberId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No member"));
        var mySessions = sessions.findAllWithAscentsOf(memberId);
        var myChecks = checks.findAllByMemberIdOrderByCreatedAt(memberId);
        var myFavorites = favorites.findAllByMemberIdOrderByCreatedAtDesc(memberId).reversed();
        var myComments = comments.findAllByAuthorIdOrderByCreatedAt(memberId);
        var myAcks = lastChanceAcks.findAllByMemberIdOrderByCreatedAt(memberId);
        var routesSet = routes.findAllSetBy(memberId);

        var routeIds = new HashSet<UUID>();
        mySessions.forEach(s -> s.ascents().forEach(a -> routeIds.add(a.routeId())));
        myChecks.forEach(c -> routeIds.add(c.routeId()));
        myFavorites.forEach(f -> routeIds.add(f.routeId()));
        myComments.forEach(c -> routeIds.add(c.routeId()));
        myAcks.forEach(a -> routeIds.add(a.routeId()));
        if (member.favoriteRouteId() != null) {
            routeIds.add(member.favoriteRouteId());
        }
        Map<UUID, String> names =
                routes.findAllById(routeIds).stream()
                        .collect(Collectors.toMap(Route::id, Route::name));
        Function<UUID, ExportedRoute> route =
                id -> id == null ? null : new ExportedRoute(id, names.get(id));

        var profile =
                new ExportedProfile(
                        member.nickname(),
                        member.firstName(),
                        member.lastName(),
                        member.email(),
                        member.role().name(),
                        member.status().name(),
                        member.birthDate(),
                        member.showAge(),
                        member.clubArrivalYear(),
                        member.climbingSinceYear(),
                        member.bio(),
                        member.appearsInRankings(),
                        route.apply(member.favoriteRouteId()),
                        member.privacyAcceptedAt(),
                        member.parentalConsentAt(),
                        member.createdAt(),
                        member.lastSeenAt());
        return new MyDataExport(
                clock.instant(),
                profile,
                mySessions.stream().map(session -> session(session, route)).toList(),
                myChecks.stream()
                        .map(
                                check ->
                                        new ExportedTick(
                                                route.apply(check.routeId()),
                                                check.mode().name(),
                                                check.source().name(),
                                                check.createdAt()))
                        .toList(),
                myFavorites.stream()
                        .map(f -> tick(route.apply(f.routeId()), f.createdAt()))
                        .toList(),
                myComments.stream()
                        .map(
                                comment ->
                                        new ExportedComment(
                                                route.apply(comment.routeId()),
                                                comment.body(),
                                                comment.createdAt(),
                                                comment.deletedAt()))
                        .toList(),
                myAcks.stream().map(a -> tick(route.apply(a.routeId()), a.createdAt())).toList(),
                routesSet.stream().map(r -> new ExportedRoute(r.id(), r.name())).toList(),
                devices.list(memberId, null).stream()
                        .map(
                                device ->
                                        new ExportedDevice(
                                                device.browser().name(),
                                                device.platform().name(),
                                                device.signedInAt(),
                                                device.lastActiveAt()))
                        .toList(),
                mediaFiles
                        .findAllByOwnerIdAndUsageIn(memberId, EnumSet.allOf(MediaUsage.class))
                        .stream()
                        .map(
                                file ->
                                        new ExportedPhoto(
                                                file.id(),
                                                file.usage().name(),
                                                file.width(),
                                                file.height(),
                                                file.createdAt()))
                        .toList());
    }

    private static ExportedSession session(
            ClimbingSession session, Function<UUID, ExportedRoute> route) {
        var ascents =
                session.ascents().stream()
                        .map(
                                ascent ->
                                        new ExportedAscent(
                                                route.apply(ascent.routeId()),
                                                ascent.mode().name(),
                                                ascent.topped(),
                                                ascent.clean(),
                                                ascent.falls(),
                                                ascent.firstTry(),
                                                ascent.feeling()))
                        .toList();
        return new ExportedSession(
                session.climbedOn(),
                session.feeling(),
                session.comment(),
                session.visibility().name(),
                session.photoId(),
                session.createdAt(),
                ascents);
    }

    private static ExportedTick tick(ExportedRoute route, Instant at) {
        return new ExportedTick(route, null, null, at);
    }
}
