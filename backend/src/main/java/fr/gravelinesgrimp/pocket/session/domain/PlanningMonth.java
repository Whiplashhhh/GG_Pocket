package fr.gravelinesgrimp.pocket.session.domain;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/** The month shown by the planning: {@code YYYY-MM}, this month by default, never a future one. */
public final class PlanningMonth {

    static final YearMonth FIRST = YearMonth.of(2000, 1);

    private PlanningMonth() {}

    /**
     * @param value {@code YYYY-MM}, or null for this month
     * @throws BusinessException {@code VALIDATION_FAILED}
     */
    public static YearMonth parse(String value, LocalDate today) {
        var current = YearMonth.from(today);
        if (value == null || value.isBlank()) {
            return current;
        }
        YearMonth month;
        try {
            month = YearMonth.parse(value.strip());
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Month is not YYYY-MM");
        }
        if (month.isAfter(current) || month.isBefore(FIRST)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Month out of range");
        }
        return month;
    }
}
