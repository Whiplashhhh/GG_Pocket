package fr.gravelinesgrimp.pocket.member.domain;

/** Names and contact as typed by the member; blanks are trimmed, an empty email is none. */
public record MemberIdentity(String nickname, String firstName, String lastName, String email) {}
