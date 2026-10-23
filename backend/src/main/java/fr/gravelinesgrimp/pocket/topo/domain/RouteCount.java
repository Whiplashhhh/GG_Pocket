package fr.gravelinesgrimp.pocket.topo.domain;

import java.util.UUID;

/** A number of things (favourites, comments…) on one route, from a grouped query. */
public record RouteCount(UUID routeId, Long count) {}
