package fr.gravelinesgrimp.pocket.seed.application;

import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.session.domain.ReactionKind;
import fr.gravelinesgrimp.pocket.session.domain.SessionComment;
import fr.gravelinesgrimp.pocket.session.domain.SessionReaction;
import fr.gravelinesgrimp.pocket.session.infra.ClimbingSessionRepository;
import fr.gravelinesgrimp.pocket.session.infra.SessionCommentRepository;
import fr.gravelinesgrimp.pocket.session.infra.SessionReactionRepository;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Demo reactions and comments (spec §17) between the three demo accounts that are friends, on their
 * latest shared sessions, so that the feed looks alive. Always the same ones (fixed seed).
 */
@Component
@Profile("dev")
class DemoFeedSeeder {

    /** The demo accounts that are all friends together (DemoSocialSeeder). */
    private static final List<String> FRIENDS = List.of("grimpeur", "direction", "ouvreur");

    private static final int SESSIONS_PER_CLIMBER = 8;

    private static final List<String> COMMENTS =
            List.of(
                    "Propre ! Tu me montres la méthode jeudi ?",
                    "La magnésie n'a pas suffi, il fallait des ventouses.",
                    "Énorme. Le dévers n'a qu'à bien se tenir.",
                    "Je valide, j'étais à l'assurage et j'ai rien eu à faire.",
                    "Tu m'avais dit séance tranquille…",
                    "Respect. Moi j'ai fait la sieste au pied de la voie.");

    private final ClimbingSessionRepository sessions;
    private final SessionReactionRepository reactions;
    private final SessionCommentRepository comments;

    DemoFeedSeeder(
            ClimbingSessionRepository sessions,
            SessionReactionRepository reactions,
            SessionCommentRepository comments) {
        this.sessions = sessions;
        this.reactions = reactions;
        this.comments = comments;
    }

    /**
     * Seeds the reactions and comments once: does nothing if there are already some.
     *
     * @return the number of reactions and comments created
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public int seed(Map<String, Member> accounts) {
        if (reactions.count() > 0 || comments.count() > 0) {
            return 0;
        }
        var random = new Random(2026);
        var created = 0;
        var kinds = ReactionKind.values();
        for (var climber : FRIENDS) {
            var shared =
                    sessions.findLatestShared(accounts.get(climber).id(), SESSIONS_PER_CLIMBER);
            for (var session : shared) {
                for (var friend : FRIENDS) {
                    if (friend.equals(climber)) {
                        continue;
                    }
                    var friendId = accounts.get(friend).id();
                    if (random.nextInt(10) < 7) {
                        reactions.save(
                                new SessionReaction(
                                        session.id(),
                                        friendId,
                                        kinds[random.nextInt(kinds.length)]));
                        created++;
                    }
                    if (random.nextInt(10) < 3) {
                        comments.save(
                                new SessionComment(
                                        session.id(),
                                        friendId,
                                        COMMENTS.get(random.nextInt(COMMENTS.size()))));
                        created++;
                    }
                }
            }
        }
        return created;
    }
}
