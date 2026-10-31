package fr.gravelinesgrimp.pocket.badge.domain;

import java.util.UUID;

/** How many times a member earned a badge code. */
public record MemberBadgeCount(UUID memberId, BadgeCode code, long count) {}
