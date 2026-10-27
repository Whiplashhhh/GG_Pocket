package fr.gravelinesgrimp.pocket.social.api;

import fr.gravelinesgrimp.pocket.social.application.MemberProfileView;
import fr.gravelinesgrimp.pocket.social.domain.FriendshipState;
import fr.gravelinesgrimp.pocket.topo.api.RouteSummaryResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.List;
import java.util.UUID;

@Schema(name = "MemberProfile", description = "age is absent unless the member chose to show it.")
public record MemberProfileResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) MemberCardResponse member,
        Integer age,
        Integer climbingSinceYear,
        String bio,
        UUID favoriteRouteId,
        @Schema(requiredMode = RequiredMode.REQUIRED) List<RouteSummaryResponse> favorites,
        @Schema(requiredMode = RequiredMode.REQUIRED) FriendshipState friendship) {

    static MemberProfileResponse from(MemberProfileView view) {
        return new MemberProfileResponse(
                MemberCardResponse.from(view.member()),
                view.age(),
                view.climbingSinceYear(),
                view.bio(),
                view.favoriteRouteId(),
                view.favorites().stream().map(RouteSummaryResponse::from).toList(),
                view.friendship());
    }
}
