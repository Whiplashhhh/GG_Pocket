package fr.gravelinesgrimp.pocket.member.application;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import fr.gravelinesgrimp.pocket.member.domain.MemberAccess;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Any member whose account the board has validated (D-107). An account awaiting validation only has
 * its own logbook: it neither sees the rest of the club (directory, profiles, podium) nor shows
 * itself there (friend requests, comments on routes).
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
@PreAuthorize("hasRole('TOURIST') and hasAuthority('" + MemberAccess.VALIDATED + "')")
public @interface ValidatedMember {}
