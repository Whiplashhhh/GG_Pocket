package fr.gravelinesgrimp.pocket.session.domain;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** A session as sent by the climber, before it is saved. Ascents are in the order of the list. */
public record SessionDraft(
        LocalDate climbedOn,
        Integer feeling,
        String comment,
        SessionVisibility visibility,
        List<AscentDraft> ascents) {

    public static final int MAX_ASCENTS = 50;
    public static final int MAX_COMMENT_LENGTH = 500;

    public SessionDraft {
        comment = comment == null || comment.isBlank() ? null : comment.strip();
        visibility = visibility == null ? SessionVisibility.FRIENDS : visibility;
        ascents = List.copyOf(ascents);
    }

    /**
     * The rules that do not depend on the date or the routes (see {@link SessionCalendar}).
     *
     * @throws BusinessException {@code VALIDATION_FAILED}, {@code INVALID_ASCENT}
     */
    public void check() {
        if (ascents.isEmpty() || ascents.size() > MAX_ASCENTS) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "1 to 50 ascents");
        }
        if (feeling != null && (feeling < Feeling.MIN || feeling > Feeling.MAX)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Feeling out of range");
        }
        if (comment != null && comment.length() > MAX_COMMENT_LENGTH) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Comment too long");
        }
        ascents.forEach(AscentDraft::check);
    }

    public Set<UUID> routeIds() {
        return ascents.stream().map(AscentDraft::routeId).collect(Collectors.toSet());
    }

    /** Whoever held the rope on at least one route. */
    public Set<UUID> belayerIds() {
        return ascents.stream()
                .map(AscentDraft::belayerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
