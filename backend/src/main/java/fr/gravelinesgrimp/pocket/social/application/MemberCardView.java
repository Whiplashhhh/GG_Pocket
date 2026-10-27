package fr.gravelinesgrimp.pocket.social.application;

import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;

import java.util.UUID;

/** A member as listed in the directory and among my friends: what any member may see. */
public record MemberCardView(
        UUID id,
        String nickname,
        String firstName,
        String lastName,
        UUID avatarId,
        Role role,
        Integer clubArrivalYear) {

    static MemberCardView of(Member member) {
        return new MemberCardView(
                member.id(),
                member.nickname(),
                member.firstName(),
                member.lastName(),
                member.avatarId(),
                member.role(),
                member.clubArrivalYear());
    }
}
