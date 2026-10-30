package fr.gravelinesgrimp.pocket.ranking.domain;

/** A line of a ranking: the rank is shared by members on the same score. */
public record RankedScore(int rank, Score score) {}
