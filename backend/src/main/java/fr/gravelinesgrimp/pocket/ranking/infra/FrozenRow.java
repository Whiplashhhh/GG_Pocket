package fr.gravelinesgrimp.pocket.ranking.infra;

import fr.gravelinesgrimp.pocket.member.domain.FormerMember;
import fr.gravelinesgrimp.pocket.member.domain.MemberStatus;
import fr.gravelinesgrimp.pocket.ranking.domain.RankedScore;
import fr.gravelinesgrimp.pocket.ranking.domain.RankingKind;
import fr.gravelinesgrimp.pocket.ranking.domain.Score;

import java.util.UUID;

/**
 * A frozen rank, with who holds it today. A rank never moves once frozen (D-103): when its holder
 * has since been disabled or left the rankings, it stays, held by "Ancien·ne membre".
 */
public record FrozenRow(
        RankingKind kind,
        int rank,
        int value,
        UUID memberId,
        String nickname,
        UUID avatarId,
        MemberStatus status,
        boolean appearsInRankings) {

    /** Still shown under their name: active and in the rankings, or already the former member. */
    public boolean named() {
        return FormerMember.is(memberId) || (status == MemberStatus.ACTIVE && appearsInRankings);
    }

    public RankedScore toRankedScore(String formerNickname) {
        var score =
                named()
                        ? new Score(memberId, nickname, avatarId, value)
                        : new Score(FormerMember.ID, formerNickname, null, value);
        return new RankedScore(rank, score);
    }
}
