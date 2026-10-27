package fr.gravelinesgrimp.pocket.social.api;

import fr.gravelinesgrimp.pocket.social.application.DirectoryView;
import fr.gravelinesgrimp.pocket.social.domain.FriendshipState;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.List;

@Schema(
        name = "Directory",
        description =
                "climberCount counts every active member, me included; members leaves me out.")
public record DirectoryResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) long climberCount,
        @Schema(requiredMode = RequiredMode.REQUIRED) List<Entry> members) {

    @Schema(name = "DirectoryEntry")
    public record Entry(
            @Schema(requiredMode = RequiredMode.REQUIRED) MemberCardResponse member,
            @Schema(requiredMode = RequiredMode.REQUIRED) FriendshipState friendship) {}

    static DirectoryResponse from(DirectoryView view) {
        return new DirectoryResponse(
                view.climberCount(),
                view.members().stream()
                        .map(
                                entry ->
                                        new Entry(
                                                MemberCardResponse.from(entry.member()),
                                                entry.friendship()))
                        .toList());
    }
}
