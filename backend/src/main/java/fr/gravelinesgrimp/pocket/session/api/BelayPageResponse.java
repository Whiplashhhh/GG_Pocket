package fr.gravelinesgrimp.pocket.session.api;

import fr.gravelinesgrimp.pocket.session.application.BelayView;
import fr.gravelinesgrimp.pocket.topo.api.MemberRefResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(name = "BelayPage", description = "nextCursor is absent on the last page.")
public record BelayPageResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) List<Item> items, String nextCursor) {

    @Schema(
            name = "Belay",
            description = "A session where I held the rope; readable says whether I may open it.")
    public record Item(
            @Schema(requiredMode = RequiredMode.REQUIRED) UUID sessionId,
            @Schema(requiredMode = RequiredMode.REQUIRED) MemberRefResponse climber,
            @Schema(requiredMode = RequiredMode.REQUIRED) LocalDate climbedOn,
            @Schema(requiredMode = RequiredMode.REQUIRED) int routeCount,
            @Schema(requiredMode = RequiredMode.REQUIRED) boolean readable) {}

    static BelayPageResponse from(BelayView.Page page) {
        return new BelayPageResponse(
                page.items().stream()
                        .map(
                                belay ->
                                        new Item(
                                                belay.sessionId(),
                                                MemberRefResponse.from(belay.climber()),
                                                belay.climbedOn(),
                                                belay.routeCount(),
                                                belay.readable()))
                        .toList(),
                page.nextCursor());
    }
}
