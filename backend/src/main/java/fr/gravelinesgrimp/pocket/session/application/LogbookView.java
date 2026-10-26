package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.session.domain.Logbook;
import fr.gravelinesgrimp.pocket.topo.application.RouteSummaryView;

import java.util.List;

/** My logbook, each route with its summary as I see it in the topo. */
public record LogbookView(
        Logbook.Totals totals, List<Logbook.GradeCount> grades, List<RouteView> routes) {

    public record RouteView(RouteSummaryView route, Logbook.RouteLine line) {}
}
