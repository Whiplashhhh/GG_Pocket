package fr.gravelinesgrimp.pocket.member.application;

import fr.gravelinesgrimp.pocket.member.domain.MemberDetails;
import fr.gravelinesgrimp.pocket.member.domain.MemberIdentity;

import java.time.LocalDate;

/** A member edits their own profile. Field formats are validated by the API layer. */
public record ProfileChange(
        MemberIdentity identity,
        MemberDetails details,
        LocalDate birthDate,
        boolean parentalConsent) {}
