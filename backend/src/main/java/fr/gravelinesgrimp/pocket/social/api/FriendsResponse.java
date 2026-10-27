package fr.gravelinesgrimp.pocket.social.api;

import fr.gravelinesgrimp.pocket.social.application.FriendsView;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.Instant;
import java.util.List;

@Schema(
        name = "Friends",
        description =
                "incoming: requests waiting for my answer; outgoing: the ones I sent. Newest first.")
public record FriendsResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) List<Friend> incoming,
        @Schema(requiredMode = RequiredMode.REQUIRED) List<Friend> outgoing,
        @Schema(requiredMode = RequiredMode.REQUIRED) List<Friend> friends) {

    @Schema(name = "Friend", description = "since: when asked, or when accepted for a friend.")
    public record Friend(
            @Schema(requiredMode = RequiredMode.REQUIRED) MemberCardResponse member,
            @Schema(requiredMode = RequiredMode.REQUIRED) Instant since) {

        static Friend from(FriendsView.FriendView view) {
            return new Friend(MemberCardResponse.from(view.member()), view.since());
        }
    }

    static FriendsResponse from(FriendsView view) {
        return new FriendsResponse(
                view.incoming().stream().map(Friend::from).toList(),
                view.outgoing().stream().map(Friend::from).toList(),
                view.friends().stream().map(Friend::from).toList());
    }
}
