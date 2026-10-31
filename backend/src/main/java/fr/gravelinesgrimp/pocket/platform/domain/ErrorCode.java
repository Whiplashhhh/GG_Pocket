package fr.gravelinesgrimp.pocket.platform.domain;

/**
 * Stable business error codes sent to the client in the {@code code} property of every Problem
 * Details response. The frontend translates them through {@code errors.<CODE>} in its i18n file.
 *
 * <p>Never rename or remove a constant once released: clients rely on these values.
 */
public enum ErrorCode {
    VALIDATION_FAILED(ErrorCategory.INVALID),
    MALFORMED_REQUEST(ErrorCategory.INVALID),
    UNAUTHENTICATED(ErrorCategory.UNAUTHENTICATED),
    ACCESS_DENIED(ErrorCategory.FORBIDDEN),
    CSRF_TOKEN_INVALID(ErrorCategory.FORBIDDEN),
    NOT_FOUND(ErrorCategory.NOT_FOUND),
    METHOD_NOT_ALLOWED(ErrorCategory.PROTOCOL),
    UNSUPPORTED_MEDIA_TYPE(ErrorCategory.PROTOCOL),
    REQUEST_REJECTED(ErrorCategory.PROTOCOL),
    PAYLOAD_TOO_LARGE(ErrorCategory.PROTOCOL),
    INTERNAL_ERROR(ErrorCategory.INTERNAL),
    RATE_LIMITED(ErrorCategory.TOO_MANY_REQUESTS),
    CONCURRENT_CHANGE(ErrorCategory.CONFLICT),

    // Sign-in and sessions
    BAD_CREDENTIALS(ErrorCategory.UNAUTHENTICATED),
    ACCOUNT_DISABLED(ErrorCategory.FORBIDDEN),

    // Registration and passwords
    REGISTRATION_CODE_INVALID(ErrorCategory.INVALID),
    NICKNAME_TAKEN(ErrorCategory.CONFLICT),
    EMAIL_TAKEN(ErrorCategory.CONFLICT),
    PASSWORD_TOO_SHORT(ErrorCategory.INVALID),
    PASSWORD_TOO_LONG(ErrorCategory.INVALID),
    PASSWORD_TOO_COMMON(ErrorCategory.INVALID),
    CURRENT_PASSWORD_WRONG(ErrorCategory.INVALID),
    PARENTAL_CONSENT_REQUIRED(ErrorCategory.INVALID),
    JOIN_NOTE_REQUIRED(ErrorCategory.INVALID),
    YEAR_IN_THE_FUTURE(ErrorCategory.INVALID),
    PASSWORD_RESET_LINK_INVALID(ErrorCategory.INVALID),

    // Photos
    IMAGE_UNSUPPORTED(ErrorCategory.INVALID),
    IMAGE_TOO_LARGE(ErrorCategory.INVALID),

    // Member management
    SELF_MANAGEMENT_FORBIDDEN(ErrorCategory.FORBIDDEN),
    LAST_ADMIN(ErrorCategory.CONFLICT),
    MEMBER_ALREADY_VALIDATED(ErrorCategory.CONFLICT),
    MEMBER_AWAITING_VALIDATION(ErrorCategory.CONFLICT),

    // Club configuration
    ALREADY_EXISTS(ErrorCategory.CONFLICT),
    CLUB_ITEM_INACTIVE(ErrorCategory.INVALID),

    // Topo
    FAVORITES_FULL(ErrorCategory.CONFLICT),
    ROUTE_DISMOUNTED(ErrorCategory.CONFLICT),
    ROUTE_STATUS_CONFLICT(ErrorCategory.CONFLICT),
    INVALID_SETTERS(ErrorCategory.INVALID),
    DATE_IN_THE_FUTURE(ErrorCategory.INVALID),
    DATE_IN_THE_PAST(ErrorCategory.INVALID),

    // Sessions
    SESSION_TOO_OLD(ErrorCategory.INVALID),
    MONTH_FROZEN(ErrorCategory.CONFLICT),
    INVALID_ASCENT(ErrorCategory.INVALID),
    CHECK_FROM_ASCENT(ErrorCategory.CONFLICT);

    private final ErrorCategory category;

    ErrorCode(ErrorCategory category) {
        this.category = category;
    }

    public ErrorCategory category() {
        return category;
    }
}
