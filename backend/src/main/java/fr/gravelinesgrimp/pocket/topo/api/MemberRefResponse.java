package fr.gravelinesgrimp.pocket.topo.api;

import fr.gravelinesgrimp.pocket.member.api.MediaUrls;
import fr.gravelinesgrimp.pocket.topo.application.MemberRefView;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.UUID;

@Schema(name = "MemberRef")
public record MemberRefResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = RequiredMode.REQUIRED) String nickname,
        String avatarUrl) {

    public static MemberRefResponse from(MemberRefView view) {
        return new MemberRefResponse(view.id(), view.nickname(), MediaUrls.of(view.avatarId()));
    }
}
