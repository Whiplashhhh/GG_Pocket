package fr.gravelinesgrimp.pocket.member.domain;

public enum MemberStatus {
    ACTIVE,
    /** Cannot sign in; all sessions were closed when the account was disabled. */
    DISABLED
}
