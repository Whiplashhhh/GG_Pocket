package fr.gravelinesgrimp.pocket.member.application;

import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;

import java.util.UUID;

/**
 * What the app shell needs about the signed-in member (header, menus, permissions).
 *
 * @param validated false while the board has not validated the account (D-107)
 * @param birthDateMissing an older account without birth date: asked at sign-in (D-119)
 * @param inClubFeed takes part in the club feed (D-120), validated and active
 */
public record CurrentMemberView(
        UUID id,
        String nickname,
        String firstName,
        String lastName,
        Role role,
        boolean validated,
        boolean birthDateMissing,
        boolean inClubFeed,
        UUID avatarId) {

    public static CurrentMemberView of(Member member) {
        return new CurrentMemberView(
                member.id(),
                member.nickname(),
                member.firstName(),
                member.lastName(),
                member.role(),
                member.isValidated(),
                member.birthDate() == null,
                member.isInClubFeed(),
                member.avatarId());
    }
}
