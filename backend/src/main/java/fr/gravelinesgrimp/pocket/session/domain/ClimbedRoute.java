package fr.gravelinesgrimp.pocket.session.domain;

import java.time.LocalDate;
import java.util.UUID;

/** A route I climbed at least once, and the last day I did. */
public record ClimbedRoute(UUID routeId, LocalDate lastClimbedOn) {}
