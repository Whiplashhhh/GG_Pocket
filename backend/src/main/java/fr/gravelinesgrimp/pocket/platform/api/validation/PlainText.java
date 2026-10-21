package fr.gravelinesgrimp.pocket.platform.api.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Text typed by a member: no control characters (NUL, escape…), which PostgreSQL refuses and which
 * would garble the screen. Tabs and line breaks are allowed; {@code null} is valid.
 */
@Documented
@Constraint(validatedBy = PlainTextValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface PlainText {

    String message() default "must not contain control characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
