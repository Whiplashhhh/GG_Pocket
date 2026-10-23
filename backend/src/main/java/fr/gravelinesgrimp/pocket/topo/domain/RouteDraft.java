package fr.gravelinesgrimp.pocket.topo.domain;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * What a setter fills in for a route. {@link #check(LocalDate)} applies the rules that need nothing
 * else; the service then checks what needs the database (grade, cord, colours and setters exist,
 * are active, setters are at least SETTER).
 *
 * @param holdColorIds one or two distinct colours, in the order chosen
 * @param setterIds members who set the route
 * @param setterNote a setter without an account; enough on its own if nobody else is named
 */
public record RouteDraft(
        String name,
        UUID gradeId,
        UUID cordId,
        List<UUID> holdColorIds,
        LocalDate openedOn,
        List<UUID> setterIds,
        String setterNote,
        String advice,
        List<RouteStyle> styles) {

    public static final int NAME_LENGTH = 60;
    public static final int SETTER_NOTE_LENGTH = 60;
    public static final int ADVICE_LENGTH = 500;
    public static final int MAX_HOLD_COLORS = 2;
    public static final int MAX_STYLES = 5;

    /**
     * @throws BusinessException {@code VALIDATION_FAILED} for a malformed draft (colours, styles,
     *     lengths), {@code DATE_IN_THE_FUTURE} if the route opens after {@code today}, {@code
     *     INVALID_SETTERS} if it names no setter at all
     */
    public void check(LocalDate today) {
        if (isBlank(name) || name.strip().length() > NAME_LENGTH) {
            throw invalid("Route name missing or too long");
        }
        if (gradeId == null || cordId == null || openedOn == null) {
            throw invalid("Grade, cord and opening date are required");
        }
        if (!distinctWithin(holdColorIds, 1, MAX_HOLD_COLORS)) {
            throw invalid("One or two distinct hold colours expected");
        }
        if (!distinctWithin(styles, 0, MAX_STYLES)) {
            throw invalid("At most " + MAX_STYLES + " distinct styles expected");
        }
        if (!isBlank(setterNote) && setterNote.strip().length() > SETTER_NOTE_LENGTH) {
            throw invalid("Setter note too long");
        }
        if (!isBlank(advice) && advice.strip().length() > ADVICE_LENGTH) {
            throw invalid("Advice too long");
        }
        if (openedOn.isAfter(today)) {
            throw new BusinessException(
                    ErrorCode.DATE_IN_THE_FUTURE, "Opened on " + openedOn + ", after today");
        }
        if (setterIds == null || setterIds.stream().anyMatch(Objects::isNull)) {
            throw invalid("Malformed setters");
        }
        if (setterIds.isEmpty() && isBlank(setterNote)) {
            throw new BusinessException(ErrorCode.INVALID_SETTERS, "No setter at all");
        }
    }

    private static boolean distinctWithin(List<?> values, int min, int max) {
        return values != null
                && values.size() >= min
                && values.size() <= max
                && values.stream().noneMatch(Objects::isNull)
                && new HashSet<>(values).size() == values.size();
    }

    static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static BusinessException invalid(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }
}
