package fr.gravelinesgrimp.pocket.member.application;

import fr.gravelinesgrimp.pocket.badge.domain.Seniority;
import fr.gravelinesgrimp.pocket.member.domain.Age;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Everything a member may see and edit about themselves. */
public record MyProfileView(
        UUID id,
        String nickname,
        String firstName,
        String lastName,
        String email,
        Role role,
        UUID avatarId,
        LocalDate birthDate,
        Integer age,
        boolean showAge,
        boolean parentalConsent,
        Integer clubArrivalYear,
        Seniority seniority,
        Integer climbingSinceYear,
        String bio,
        boolean appearsInRankings,
        UUID favoriteRouteId,
        Instant createdAt) {

    public static MyProfileView of(Member member, LocalDate today) {
        return new MyProfileView(
                member.id(),
                member.nickname(),
                member.firstName(),
                member.lastName(),
                member.email(),
                member.role(),
                member.avatarId(),
                member.birthDate(),
                member.birthDate() == null ? null : Age.yearsOn(member.birthDate(), today),
                member.showAge(),
                member.hasParentalConsent(),
                member.clubArrivalYear(),
                Seniority.of(member.clubArrivalYear(), today.getYear()),
                member.climbingSinceYear(),
                member.bio(),
                member.appearsInRankings(),
                member.favoriteRouteId(),
                member.createdAt());
    }
}
