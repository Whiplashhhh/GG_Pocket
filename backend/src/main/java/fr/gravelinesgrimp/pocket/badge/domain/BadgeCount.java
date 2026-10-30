package fr.gravelinesgrimp.pocket.badge.domain;

import java.time.LocalDate;

/** How many times a climber earned a badge, and when first and last (spec §7 UserBadge). */
public record BadgeCount(
        BadgeCode code, long count, LocalDate firstEarnedOn, LocalDate lastEarnedOn) {}
