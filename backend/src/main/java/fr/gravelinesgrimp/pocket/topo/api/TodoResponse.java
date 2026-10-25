package fr.gravelinesgrimp.pocket.topo.api;

import fr.gravelinesgrimp.pocket.topo.application.TodoView;
import fr.gravelinesgrimp.pocket.topo.domain.TodoState;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.List;

@Schema(
        name = "Todo",
        description =
                "Every route on the wall, easiest first. A route is on my list in a mode while its"
                        + " state is TODO; known in lead means known in top rope (FROM_LEAD).")
public record TodoResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) int routeCount,
        @Schema(requiredMode = RequiredMode.REQUIRED) int topRopeKnown,
        @Schema(requiredMode = RequiredMode.REQUIRED) int leadKnown,
        @Schema(requiredMode = RequiredMode.REQUIRED) List<RouteItem> routes) {

    @Schema(name = "TodoRoute")
    public record RouteItem(
            @Schema(requiredMode = RequiredMode.REQUIRED) RouteSummaryResponse route,
            @Schema(requiredMode = RequiredMode.REQUIRED) TodoState topRope,
            @Schema(requiredMode = RequiredMode.REQUIRED) TodoState lead) {}

    static TodoResponse from(TodoView view) {
        return new TodoResponse(
                view.routeCount(),
                view.topRopeKnown(),
                view.leadKnown(),
                view.routes().stream()
                        .map(
                                todo ->
                                        new RouteItem(
                                                RouteSummaryResponse.from(todo.route()),
                                                todo.topRope(),
                                                todo.lead()))
                        .toList());
    }
}
