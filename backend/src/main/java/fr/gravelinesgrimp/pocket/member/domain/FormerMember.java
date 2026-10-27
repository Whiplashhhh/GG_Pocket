package fr.gravelinesgrimp.pocket.member.domain;

import java.util.UUID;

/**
 * "Ancien·ne membre" (spec §14, DECISIONS D-096): the anonymous member that inherits the sessions
 * and the routes of deleted accounts (migration V10). Never listed, never managed, never signed in.
 */
public final class FormerMember {

    public static final UUID ID = UUID.fromString("00000000-0000-0000-0000-00000000a5c1");

    private FormerMember() {}

    public static boolean is(UUID memberId) {
        return ID.equals(memberId);
    }
}
