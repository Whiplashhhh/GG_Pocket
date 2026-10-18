package fr.gravelinesgrimp.pocket.platform.domain;

/**
 * Family of an {@link ErrorCode}. The API layer maps each category to an HTTP status, so business
 * code never deals with HTTP directly.
 */
public enum ErrorCategory {
    /** The request content breaks a rule (400). */
    INVALID,
    /** Nobody is logged in (401). */
    UNAUTHENTICATED,
    /** Logged in, but not allowed (403). */
    FORBIDDEN,
    /** The target does not exist or is not visible to the caller (404). */
    NOT_FOUND,
    /** The request conflicts with the current state (409). */
    CONFLICT,
    /** HTTP-level problem (405, 415…): the status comes from the framework. */
    PROTOCOL,
    /** Unexpected failure (500). */
    INTERNAL
}
