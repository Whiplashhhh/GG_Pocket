package fr.gravelinesgrimp.pocket.ranking.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

/**
 * A rank of a frozen month (spec §9.2), written once by the monthly closure. Only an account
 * deletion touches it afterwards, handing the rank over to "Ancien·ne membre".
 */
@Entity
@Table(name = "monthly_ranking_snapshot")
public class MonthlyRankingSnapshot extends TimestampedEntity {

    /** The first day of the month. */
    @Column(nullable = false, updatable = false)
    private LocalDate month;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 16)
    private RankingKind kind;

    @Column(nullable = false, updatable = false)
    private int rank;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(nullable = false, updatable = false)
    private int value;

    protected MonthlyRankingSnapshot() {
        // for JPA
    }

    public MonthlyRankingSnapshot(YearMonth month, RankingKind kind, RankedScore line) {
        this.month = month.atDay(1);
        this.kind = kind;
        this.rank = line.rank();
        this.memberId = line.score().memberId();
        this.value = line.score().value();
    }

    public YearMonth month() {
        return YearMonth.from(month);
    }

    public RankingKind kind() {
        return kind;
    }

    public int rank() {
        return rank;
    }

    public UUID memberId() {
        return memberId;
    }

    public int value() {
        return value;
    }
}
