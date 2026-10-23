package fr.gravelinesgrimp.pocket.topo.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** The route sheet: the summary, plus what the sheet and the edit form need. */
public record RouteDetailView(
        RouteSummaryView route,
        UUID gradeId,
        UUID cordId,
        List<MemberRefView> setters,
        String setterNote,
        String advice,
        Instant createdAt) {}
