package fr.gravelinesgrimp.pocket.admin.api;

import fr.gravelinesgrimp.pocket.admin.application.ModerationService;
import fr.gravelinesgrimp.pocket.admin.application.ModerationService.CommentKind;
import fr.gravelinesgrimp.pocket.admin.application.ModerationService.ModeratedCommentView;
import fr.gravelinesgrimp.pocket.topo.api.MemberRefResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * The moderation list (spec §8.11). Deleting goes through {@code deleteRouteComment} or {@code
 * deleteSessionComment}, by kind.
 */
@RestController
@RequestMapping(path = "/api/v1/admin/moderation", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Moderation")
class ModerationController {

    private static final String DEFAULT_PAGE_SIZE = "30";

    @Schema(
            name = "ModeratedComment",
            description =
                    "route for a comment on a route; climber and climbedOn for a comment on a"
                            + " session.")
    record ModeratedCommentResponse(
            @Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
            @Schema(requiredMode = RequiredMode.REQUIRED) CommentKind kind,
            @Schema(requiredMode = RequiredMode.REQUIRED) MemberRefResponse author,
            @Schema(requiredMode = RequiredMode.REQUIRED) String body,
            @Schema(requiredMode = RequiredMode.REQUIRED) Instant createdAt,
            ModeratedRouteResponse route,
            MemberRefResponse climber,
            LocalDate climbedOn) {

        static ModeratedCommentResponse from(ModeratedCommentView view) {
            return new ModeratedCommentResponse(
                    view.id(),
                    view.kind(),
                    MemberRefResponse.from(view.author()),
                    view.body(),
                    view.createdAt(),
                    view.route() == null
                            ? null
                            : new ModeratedRouteResponse(view.route().id(), view.route().name()),
                    view.climber() == null ? null : MemberRefResponse.from(view.climber()),
                    view.climbedOn());
        }
    }

    @Schema(name = "ModeratedRoute")
    record ModeratedRouteResponse(
            @Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
            @Schema(requiredMode = RequiredMode.REQUIRED) String name) {}

    @Schema(name = "ModerationPage", description = "Newest first.")
    record ModerationPageResponse(
            @Schema(requiredMode = RequiredMode.REQUIRED) List<ModeratedCommentResponse> comments,
            @Schema(description = "Pass it as ?cursor= for the next page; absent on the last page")
                    String nextCursor) {}

    private final ModerationService service;

    ModerationController(ModerationService service) {
        this.service = service;
    }

    @GetMapping("/comments")
    @Operation(
            operationId = "listModeratedComments",
            summary = "The latest comments on routes and sessions, newest first (board+)")
    ModerationPageResponse comments(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
        var page = service.latestComments(cursor, size);
        return new ModerationPageResponse(
                page.comments().stream().map(ModeratedCommentResponse::from).toList(),
                page.nextCursor());
    }
}
