package fr.gravelinesgrimp.pocket.badge.infra;

import fr.gravelinesgrimp.pocket.badge.domain.BadgeAward;
import fr.gravelinesgrimp.pocket.badge.domain.BadgeCount;
import fr.gravelinesgrimp.pocket.badge.domain.MemberBadgeCount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface BadgeAwardRepository extends JpaRepository<BadgeAward, UUID> {

    List<BadgeAward> findAllByMemberId(UUID memberId);

    /** The badges earned at least once. */
    @Query(
            "select new fr.gravelinesgrimp.pocket.badge.domain.BadgeCount(b.code, count(b),"
                    + " min(b.earnedOn), max(b.earnedOn))"
                    + " from BadgeAward b where b.memberId = :memberId group by b.code")
    List<BadgeCount> countPerCode(UUID memberId);

    /** Callers never pass an empty collection. */
    @Query(
            "select new fr.gravelinesgrimp.pocket.badge.domain.MemberBadgeCount(b.memberId,"
                    + " b.code, count(b))"
                    + " from BadgeAward b where b.memberId in :memberIds group by b.memberId, b.code")
    List<MemberBadgeCount> countPerMemberAndCode(Collection<UUID> memberIds);

    /** Callers never pass an empty collection. */
    @Query(
            "select b from BadgeAward b where b.sessionId in :sessionIds"
                    + " order by b.earnedOn, b.awardKey")
    List<BadgeAward> findAllBySessionIdIn(Collection<UUID> sessionIds);
}
