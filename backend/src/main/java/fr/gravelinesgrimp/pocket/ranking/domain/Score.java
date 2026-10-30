package fr.gravelinesgrimp.pocket.ranking.domain;

import java.util.UUID;

/** What a member scored in a month's ranking, before ranks are given. */
public record Score(UUID memberId, String nickname, UUID avatarId, int value) {}
