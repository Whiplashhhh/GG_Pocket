package fr.gravelinesgrimp.pocket.member.domain;

/** Optional profile details, all editable by the member. */
public record MemberDetails(
        Integer clubArrivalYear,
        Integer climbingSinceYear,
        String bio,
        boolean showAge,
        boolean appearsInRankings) {}
