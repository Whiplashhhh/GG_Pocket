package fr.gravelinesgrimp.pocket.session.api;

import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;
import fr.gravelinesgrimp.pocket.session.application.SessionCommentService;
import fr.gravelinesgrimp.pocket.session.application.SessionReactionService;
import fr.gravelinesgrimp.pocket.session.application.SessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * The feed and what friends say about a session. A session I cannot read is "not found" (checked by
 * the services).
 */
@RestController
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Feed")
class SessionSocialController {

    private static final String DEFAULT_PAGE_SIZE = "10";

    private final SessionService sessions;
    private final SessionReactionService reactions;
    private final SessionCommentService comments;

    SessionSocialController(
            SessionService sessions,
            SessionReactionService reactions,
            SessionCommentService comments) {
        this.sessions = sessions;
        this.reactions = reactions;
        this.comments = comments;
    }

    @GetMapping("/feed")
    @Operation(
            operationId = "getFeed",
            summary =
                    "My sessions and the ones my friends share, latest day first (cursor"
                            + " pagination)")
    SessionPageResponse feed(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
        return SessionPageResponse.from(sessions.feed(me.memberId(), cursor, size));
    }

    @PutMapping("/sessions/{id}/reaction")
    @Operation(
            operationId = "reactToSession",
            summary = "Give or change my reaction to someone else's session")
    SessionReactionsResponse react(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id,
            @Valid @RequestBody SessionReactionRequest body) {
        return SessionReactionsResponse.from(reactions.react(me.memberId(), id, body.kind()));
    }

    @DeleteMapping("/sessions/{id}/reaction")
    @Operation(operationId = "takeBackSessionReaction", summary = "Take back my reaction")
    SessionReactionsResponse takeBack(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id) {
        return SessionReactionsResponse.from(reactions.takeBack(me.memberId(), id));
    }

    @GetMapping("/sessions/{id}/comments")
    @Operation(operationId = "listSessionComments", summary = "Comments on a session, oldest first")
    List<SessionCommentResponse> listComments(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id) {
        return comments.list(me.memberId(), id).stream().map(SessionCommentResponse::from).toList();
    }

    @PostMapping("/sessions/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "addSessionComment", summary = "Comment on a session")
    SessionCommentResponse addComment(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id,
            @Valid @RequestBody SessionCommentRequest body) {
        return SessionCommentResponse.from(comments.add(me.memberId(), id, body.body()));
    }

    @DeleteMapping("/session-comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            operationId = "deleteSessionComment",
            summary = "Delete my comment, or anyone's (board+)")
    void deleteComment(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id) {
        comments.delete(me.memberId(), id);
    }
}
