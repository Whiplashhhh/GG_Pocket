package fr.gravelinesgrimp.pocket.auth.application;

import fr.gravelinesgrimp.pocket.member.domain.MemberIdentity;

import java.time.LocalDate;

/** What someone fills in to join (spec §6). Field formats are validated by the API layer. */
public record Registration(
        String code,
        MemberIdentity identity,
        String password,
        LocalDate birthDate,
        boolean parentalConsent) {}
