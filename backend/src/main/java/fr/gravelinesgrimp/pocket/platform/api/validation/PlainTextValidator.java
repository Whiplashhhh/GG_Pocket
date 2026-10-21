package fr.gravelinesgrimp.pocket.platform.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Checks {@link PlainText}. */
public class PlainTextValidator implements ConstraintValidator<PlainText, CharSequence> {

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        return value == null || value.codePoints().noneMatch(PlainTextValidator::isForbidden);
    }

    static boolean isForbidden(int codePoint) {
        return Character.getType(codePoint) == Character.CONTROL
                && codePoint != '\t'
                && codePoint != '\n'
                && codePoint != '\r';
    }
}
