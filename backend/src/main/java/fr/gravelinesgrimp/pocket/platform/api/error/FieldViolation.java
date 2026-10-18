package fr.gravelinesgrimp.pocket.platform.api.error;

/**
 * One invalid field in a {@code VALIDATION_FAILED} problem.
 *
 * @param field name of the field as sent by the client (for instance {@code nickname})
 * @param code constraint that failed (for instance {@code NotBlank}, {@code Size})
 */
public record FieldViolation(String field, String code) {}
