package fr.gravelinesgrimp.pocket.topo.application;

import fr.gravelinesgrimp.pocket.topo.domain.TodoState;

import java.util.List;

/** My "à faire" list: every route on the wall with where I stand on it in each mode. */
public record TodoView(int routeCount, int topRopeKnown, int leadKnown, List<RouteTodo> routes) {

    public record RouteTodo(RouteSummaryView route, TodoState topRope, TodoState lead) {}
}
