package fr.gravelinesgrimp.pocket.badge.infra;

import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

/** The whole climbing history of a member, as the badge engine reads it. */
public interface BadgeHistoryRepository extends Repository<ClimbingSession, UUID> {

    /** Every session, whatever its visibility, its ascents in order. */
    @Query(
            "select new fr.gravelinesgrimp.pocket.badge.infra.HistoryRow(s.id, s.climbedOn,"
                    + " s.createdAt, a.routeId, a.mode, a.topped, a.clean, a.firstTry, g.code,"
                    + " r.openedOn)"
                    + " from ClimbingSession s left join s.ascents a"
                    + " left join Route r on r.id = a.routeId"
                    + " left join Grade g on g.id = r.gradeId"
                    + " where s.memberId = :memberId"
                    + " order by s.climbedOn, s.createdAt, s.id, a.position")
    List<HistoryRow> historyOf(UUID memberId);
}
