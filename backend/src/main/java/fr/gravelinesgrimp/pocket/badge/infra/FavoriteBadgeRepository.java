package fr.gravelinesgrimp.pocket.badge.infra;

import fr.gravelinesgrimp.pocket.badge.domain.FavoriteBadge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface FavoriteBadgeRepository extends JpaRepository<FavoriteBadge, UUID> {

    List<FavoriteBadge> findAllByMemberIdOrderBySlot(UUID memberId);

    /** Callers never pass an empty collection. */
    @Query(
            "select f from FavoriteBadge f where f.memberId in :memberIds"
                    + " order by f.memberId, f.slot")
    List<FavoriteBadge> findAllByMemberIdIn(Collection<UUID> memberIds);

    /** Right away, so that the new choice can be inserted in the same transaction. */
    @Modifying(flushAutomatically = true)
    @Query("delete from FavoriteBadge f where f.memberId = :memberId")
    void deleteAllOf(UUID memberId);
}
