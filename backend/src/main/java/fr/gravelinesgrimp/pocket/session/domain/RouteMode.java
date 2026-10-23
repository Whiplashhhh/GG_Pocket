package fr.gravelinesgrimp.pocket.session.domain;

import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import java.util.UUID;

/** A route climbed in one mode. */
public record RouteMode(UUID routeId, ClimbMode mode) {}
