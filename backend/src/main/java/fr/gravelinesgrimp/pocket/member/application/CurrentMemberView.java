package fr.gravelinesgrimp.pocket.member.application;

import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;

import java.util.UUID;

/** What the app shell needs about the signed-in member (header, menus, permissions). */
public record CurrentMemberView(
        UUID id, String nickname, String firstName, String lastName, Role role, UUID avatarId) {

    public static CurrentMemberView of(Member member) {
        return new CurrentMemberView(
                member.id(),
                member.nickname(),
                member.firstName(),
                member.lastName(),
                member.role(),
                member.avatarId());
    }
}
