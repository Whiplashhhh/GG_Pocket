package fr.gravelinesgrimp.pocket.session.api;

import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;
import fr.gravelinesgrimp.pocket.session.application.SessionPhotoService;
import fr.gravelinesgrimp.pocket.session.application.SessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/** My sessions at the gym. Someone else's session is "not found" (checked by the services). */
@RestController
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Sessions")
class SessionController {

    private static final String DEFAULT_PAGE_SIZE = "10";

    private final SessionService sessions;
    private final SessionPhotoService photos;

    SessionController(SessionService sessions, SessionPhotoService photos) {
        this.sessions = sessions;
        this.photos = photos;
    }

    @PostMapping("/sessions")
    @Operation(
            operationId = "createSession",
            summary = "Log a session (200 with the saved one if its clientRef was already sent)")
    @ApiResponse(responseCode = "201", description = "Saved")
    @ApiResponse(responseCode = "200", description = "Already saved with this clientRef")
    ResponseEntity<SessionResponse> create(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @Valid @RequestBody SessionRequest body) {
        var saved = sessions.create(me.memberId(), body.clientRef(), body.toDraft());
        return ResponseEntity.status(saved.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(SessionResponse.from(saved.session()));
    }

    @GetMapping("/sessions/{id}")
    @Operation(operationId = "getSession", summary = "One of my sessions")
    SessionResponse get(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id) {
        return SessionResponse.from(sessions.get(me.memberId(), id));
    }

    @PutMapping("/sessions/{id}")
    @Operation(operationId = "updateSession", summary = "Change one of my sessions (month open)")
    SessionResponse update(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id,
            @Valid @RequestBody SessionRequest body) {
        return SessionResponse.from(sessions.update(me.memberId(), id, body.toDraft()));
    }

    @DeleteMapping("/sessions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteSession", summary = "Delete one of my sessions (month open)")
    void delete(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id) {
        sessions.delete(me.memberId(), id);
    }

    @PutMapping(path = "/sessions/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            operationId = "replaceSessionPhoto",
            summary = "Upload the session photo (JPEG, PNG or WebP, 10 MB max)")
    SessionResponse replacePhoto(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file)
            throws IOException {
        return SessionResponse.from(photos.replace(me.memberId(), id, file.getBytes()));
    }

    @DeleteMapping("/sessions/{id}/photo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteSessionPhoto", summary = "Remove the session photo")
    void deletePhoto(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id) {
        photos.remove(me.memberId(), id);
    }

    @GetMapping("/me/sessions")
    @Operation(
            operationId = "listMySessions",
            summary = "My sessions, latest day first (cursor pagination)")
    SessionPageResponse listMine(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
        return SessionPageResponse.from(sessions.listMine(me.memberId(), cursor, size));
    }

    @GetMapping("/members/{id}/sessions")
    @Operation(
            operationId = "listMemberSessions",
            summary =
                    "A friend's sessions shared with friends, latest day first (cursor pagination)")
    SessionPageResponse listOf(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
        return SessionPageResponse.from(sessions.listOf(me.memberId(), id, cursor, size));
    }

    @GetMapping("/me/climbed-routes")
    @Operation(
            operationId = "listMyClimbedRoutes",
            summary = "Routes I climbed, most recently climbed first")
    List<ClimbedRouteResponse> listClimbedRoutes(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me) {
        return sessions.climbedRoutes(me.memberId()).stream()
                .map(ClimbedRouteResponse::from)
                .toList();
    }
}
