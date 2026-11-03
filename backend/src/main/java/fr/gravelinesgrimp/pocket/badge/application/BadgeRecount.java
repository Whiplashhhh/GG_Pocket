package fr.gravelinesgrimp.pocket.badge.application;

import fr.gravelinesgrimp.pocket.badge.infra.BadgeHistoryRepository;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

/**
 * Replays the badges of every climber at start-up, so that a change of the rules (D-111: the grades
 * earned one by one) reaches the badges already stored without waiting for a new session. The
 * engine is pure and the write is a difference: a start-up with unchanged rules writes nothing.
 * Each climber in their own transaction, their row locked like a change of their sessions; after
 * the demo seed (order 0), which writes its badges itself.
 */
@Component
@Order(1)
public class BadgeRecount implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BadgeRecount.class);

    private final BadgeHistoryRepository history;
    private final MemberRepository members;
    private final BadgeAwards awards;
    private final TransactionTemplate transactions;

    public BadgeRecount(
            BadgeHistoryRepository history,
            MemberRepository members,
            BadgeAwards awards,
            PlatformTransactionManager transactionManager) {
        this.history = history;
        this.members = members;
        this.awards = awards;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        var climbers = history.climbers();
        var failed = 0;
        for (var memberId : climbers) {
            try {
                transactions.executeWithoutResult(status -> recount(memberId));
            } catch (RuntimeException e) {
                // One climber's badges must not keep the club out of the app: the next change of
                // their sessions replays them again.
                failed++;
                log.warn("Badge recount failed for member {}", memberId, e);
            }
        }
        log.info("Badges replayed for {} climbers ({} failed)", climbers.size(), failed);
    }

    private void recount(UUID memberId) {
        if (members.lockById(memberId).isPresent()) {
            awards.refresh(memberId);
        }
    }
}
