package fr.gravelinesgrimp.pocket.notification.api;

import fr.gravelinesgrimp.pocket.notification.application.NotificationService.NotificationView;
import fr.gravelinesgrimp.pocket.notification.application.NotificationService.NotificationsView;
import fr.gravelinesgrimp.pocket.notification.domain.NotificationKind;
import fr.gravelinesgrimp.pocket.session.domain.ReactionKind;
import fr.gravelinesgrimp.pocket.topo.api.MemberRefResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(name = "Notifications")
public record NotificationsResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "Newest first, a month back")
                List<Item> items,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "How many came since I last opened the bell")
                int unreadCount) {

    @Schema(name = "Notification")
    public record Item(
            @Schema(requiredMode = RequiredMode.REQUIRED, description = "Stable, for the list keys")
                    String id,
            @Schema(requiredMode = RequiredMode.REQUIRED) NotificationKind kind,
            @Schema(requiredMode = RequiredMode.REQUIRED) Instant at,
            @Schema(requiredMode = RequiredMode.REQUIRED) MemberRefResponse actor,
            @Schema(description = "The session concerned; absent for a friendship") UUID sessionId,
            @Schema(description = "Its day; absent for a friendship") LocalDate climbedOn,
            @Schema(description = "For a reaction") ReactionKind reaction,
            @Schema(description = "The start of a comment or an answer") String excerpt,
            @Schema(requiredMode = RequiredMode.REQUIRED, description = "New since I last looked")
                    boolean unread) {

        static Item from(NotificationView view) {
            return new Item(
                    view.id(),
                    view.kind(),
                    view.at(),
                    MemberRefResponse.from(view.actor()),
                    view.sessionId(),
                    view.climbedOn(),
                    view.reaction(),
                    view.excerpt(),
                    view.unread());
        }
    }

    static NotificationsResponse from(NotificationsView view) {
        return new NotificationsResponse(
                view.items().stream().map(Item::from).toList(), view.unreadCount());
    }
}
