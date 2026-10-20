package fr.gravelinesgrimp.pocket.auth.application;

import fr.gravelinesgrimp.pocket.platform.domain.RateLimitedException;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Sliding-window attempt counters, kept in memory: one API instance serves the club, and a restart
 * resetting the counters is acceptable (DECISIONS D-051). Keys are client addresses or nicknames.
 */
@Component
public class RateLimiter {

    /** At most {@code maxAttempts} within any {@code window}. */
    public record Rule(String name, int maxAttempts, Duration window) {}

    /** Generous: the whole gym may share one Wi-Fi address. */
    public static final Rule LOGIN_PER_ADDRESS =
            new Rule("login-per-address", 30, Duration.ofMinutes(15));

    /** Failed passwords only: a member who types it right is never slowed down by others. */
    public static final Rule LOGIN_FAILURES_PER_NICKNAME =
            new Rule("login-failures-per-nickname", 10, Duration.ofMinutes(15));

    public static final Rule REGISTRATION_CODE_PER_ADDRESS =
            new Rule("registration-code-per-address", 20, Duration.ofMinutes(15));

    public static final Rule REGISTRATION_PER_ADDRESS =
            new Rule("registration-per-address", 30, Duration.ofHours(1));

    public static final Rule PASSWORD_RESET_PER_ADDRESS =
            new Rule("password-reset-per-address", 20, Duration.ofMinutes(15));

    /** Above this many tracked keys, stale ones are dropped before adding more. */
    private static final int PURGE_THRESHOLD = 10_000;

    private final Map<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();
    private final Clock clock;

    public RateLimiter(Clock clock) {
        this.clock = clock;
    }

    /** Counts this attempt, or refuses it if the limit is already reached. */
    public void consume(Rule rule, String key) {
        var now = clock.instant();
        var refusedUntil = new AtomicReference<Instant>();
        attempts.compute(
                id(rule, key),
                (id, recent) -> {
                    var kept = prune(recent, rule, now);
                    if (kept.size() >= rule.maxAttempts()) {
                        refusedUntil.set(kept.peekFirst().plus(rule.window()));
                    } else {
                        kept.addLast(now);
                    }
                    return kept;
                });
        throwIfRefused(rule, refusedUntil.get(), now);
        purgeIfLarge(now);
    }

    /** Refuses if the limit is reached, without counting this attempt. */
    public void ensureAllowed(Rule rule, String key) {
        var now = clock.instant();
        var refusedUntil = new AtomicReference<Instant>();
        attempts.computeIfPresent(
                id(rule, key),
                (id, recent) -> {
                    var kept = prune(recent, rule, now);
                    if (kept.size() >= rule.maxAttempts()) {
                        refusedUntil.set(kept.peekFirst().plus(rule.window()));
                    }
                    return kept.isEmpty() ? null : kept;
                });
        throwIfRefused(rule, refusedUntil.get(), now);
    }

    /** Counts an attempt without checking (for instance a failed password). */
    public void record(Rule rule, String key) {
        var now = clock.instant();
        attempts.compute(
                id(rule, key),
                (id, recent) -> {
                    var kept = prune(recent, rule, now);
                    kept.addLast(now);
                    return kept;
                });
        purgeIfLarge(now);
    }

    /** Forgets the attempts of a key (for instance after a successful sign-in). */
    public void reset(Rule rule, String key) {
        attempts.remove(id(rule, key));
    }

    private static String id(Rule rule, String key) {
        return rule.name() + "|" + key;
    }

    private static Deque<Instant> prune(Deque<Instant> recent, Rule rule, Instant now) {
        var kept = recent == null ? new ArrayDeque<Instant>() : recent;
        var oldestKept = now.minus(rule.window());
        while (!kept.isEmpty() && !kept.peekFirst().isAfter(oldestKept)) {
            kept.removeFirst();
        }
        return kept;
    }

    private static void throwIfRefused(Rule rule, Instant refusedUntil, Instant now) {
        if (refusedUntil != null) {
            throw new RateLimitedException(rule.name(), Duration.between(now, refusedUntil));
        }
    }

    /** Every window here is 1 hour or less: an entry older than that is useless. */
    private void purgeIfLarge(Instant now) {
        if (attempts.size() > PURGE_THRESHOLD) {
            var stale = now.minus(Duration.ofHours(1));
            attempts.entrySet()
                    .removeIf(
                            entry -> {
                                var last = entry.getValue().peekLast();
                                return last == null || last.isBefore(stale);
                            });
        }
    }
}
