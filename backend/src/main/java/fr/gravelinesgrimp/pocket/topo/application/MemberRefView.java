package fr.gravelinesgrimp.pocket.topo.application;

import fr.gravelinesgrimp.pocket.member.domain.Member;

import java.util.UUID;

/** A member as shown next to their words or their routes: nickname and photo. */
public record MemberRefView(UUID id, String nickname, UUID avatarId) {

    static MemberRefView of(Member member) {
        return new MemberRefView(member.id(), member.nickname(), member.avatarId());
    }
}
