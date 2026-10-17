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
    INTERNAL_ERROR(ErrorCategory.INTERNAL);

    private final ErrorCategory category;

    ErrorCode(ErrorCategory category) {
        this.category = category;
    }

    public ErrorCategory category() {
        return category;
    }
}
