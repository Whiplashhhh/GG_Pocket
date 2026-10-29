package fr.gravelinesgrimp.pocket.session.domain;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import java.util.UUID;

/**
 * One try of a route, as sent by the climber. A clean ascent ("enchaînée") is topped, without a
 * fall; {@code firstTry} is what the climber says (the app fills it in when they never tried the
 * route before). {@code belayerId} is who held the rope, if the climber says so: a club member,
 * checked by the service.
 */
public record AscentDraft(
        UUID routeId,
        ClimbMode mode,
        boolean topped,
        boolean clean,
        int falls,
        boolean firstTry,
        Integer feeling,
        UUID belayerId) {

    public static final int MAX_FALLS = 99;

    /** Without a belayer. */
    public AscentDraft(
            UUID routeId,
            ClimbMode mode,
            boolean topped,
            boolean clean,
            int falls,
            boolean firstTry,
            Integer feeling) {
        this(routeId, mode, topped, clean, falls, firstTry, feeling, null);
    }

    /**
     * @throws BusinessException {@code INVALID_ASCENT}
     */
    public void check() {
        if (falls < 0 || falls > MAX_FALLS) {
            throw invalid("Falls out of range");
        }
        if (clean && (!topped || falls > 0)) {
            throw invalid("A clean ascent is topped without a fall");
        }
        if (feeling != null && (feeling < Feeling.MIN || feeling > Feeling.MAX)) {
            throw invalid("Feeling out of range");
        }
    }

    private static BusinessException invalid(String message) {
        return new BusinessException(ErrorCode.INVALID_ASCENT, message);
    }
}
