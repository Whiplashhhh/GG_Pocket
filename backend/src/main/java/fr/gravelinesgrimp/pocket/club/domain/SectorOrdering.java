package fr.gravelinesgrimp.pocket.club.domain;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/** A new order of the sectors names each of them exactly once. */
public final class SectorOrdering {

    private SectorOrdering() {}

    /**
     * @throws BusinessException {@code VALIDATION_FAILED} if a sector is missing, repeated or
     *     unknown
     */
    public static void check(List<UUID> requested, Collection<UUID> existing) {
        var distinct = new HashSet<>(requested);
        if (distinct.size() != requested.size()
                || requested.size() != existing.size()
                || !distinct.containsAll(existing)) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "The new order must list every sector exactly once");
        }
    }
}
