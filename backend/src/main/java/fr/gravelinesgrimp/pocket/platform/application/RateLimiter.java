package fr.gravelinesgrimp.pocket.platform.application;

import fr.gravelinesgrimp.pocket.platform.domain.RateLimitedException;

import org.springframework.beans.factory.annotation.Autowired;
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
 * resetting the counters is acceptable (DECISIONS D-050). Keys are client addresses, nicknames or
 * member ids.
 */
@Component
public class RateLimiter {

    /** At most {@code maxAttempts} within any {@code window}. */
    public record Rule(String name, int maxAttempts, Duration window) {}

    /** Generous: the whole gym may share one Wi-Fi address. */
    public static final Rule LOGIN_PER_ADDRESS =
            new Rule("login-per-address", 30, Duration.ofMinutes(15));

    /**
     * Failed passwords for one nickname from one address: stops guessing, without letting a
     * stranger lock the account for everyone (their failures only block their own address).
     */
    public static final Rule LOGIN_FAILURES_PER_NICKNAME_AND_ADDRESS =
            new Rule("login-failures-per-nickname-and-address", 10, Duration.ofMinutes(15));

    /** Failed passwords for one nickname from anywhere: only a wide, distributed attack hits it. */
    public static final Rule LOGIN_FAILURES_PER_NICKNAME =
            new Rule("login-failures-per-nickname", 100, Duration.ofMinutes(15));

    public static final Rule REGISTRATION_CODE_PER_ADDRESS =
            new Rule("registration-code-per-address", 20, Duration.ofMinutes(15));

    public static final Rule REGISTRATION_PER_ADDRESS =
            new Rule("registration-per-address", 30, Duration.ofHours(1));

    public static final Rule PASSWORD_RESET_PER_ADDRESS =
            new Rule("password-reset-per-address", 20, Duration.ofMinutes(15));

    /** Decoding a photo is the heaviest thing a member can ask for. */
    public static final Rule AVATAR_UPLOAD_PER_MEMBER =
            new Rule("avatar-upload-per-member", 10, Duration.ofHours(1));

    /** A setter photographs a whole new wall in one go. */
    public static final Rule ROUTE_PHOTO_UPLOAD_PER_MEMBER =
            new Rule("route-photo-upload-per-member", 40, Duration.ofHours(1));

    public static final Rule ROUTE_COMMENT_PER_MEMBER =
            new Rule("route-comment-per-member", 20, Duration.ofHours(1));

    /** Creating, changing or deleting sessions: far above a real day at the gym. */
    public static final Rule SESSION_WRITE_PER_MEMBER =
            new Rule("session-write-per-member", 60, Duration.ofHours(1));

    public static final Rule SESSION_PHOTO_UPLOAD_PER_MEMBER =
            new Rule("session-photo-upload-per-member", 20, Duration.ofHours(1));

    /** Above this many tracked keys, stale ones are dropped before adding more. */
    private static final int PURGE_THRESHOLD = 10_000;

    /**
     * Hard ceiling on tracked keys, so a flood of addresses cannot exhaust the memory: beyond it,
     * the keys quiet for the longest time are forgotten first (a few megabytes at most). Client
     * addresses are already grouped by IPv6 /64 (ClientAddress), so one connection cannot fill it.
     */
    static final int MAX_TRACKED_KEYS = 50_000;

    private final Map<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();
    private final Clock clock;
    private final int purgeThreshold;
    private final int maxTrackedKeys;

    @Autowired
    public RateLimiter(Clock clock) {
        this(clock, PURGE_THRESHOLD, MAX_TRACKED_KEYS);
    }

    RateLimiter(Clock clock, int purgeThreshold, int maxTrackedKeys) {
        this.clock = clock;
        this.purgeThreshold = purgeThreshold;
        this.maxTrackedKeys = maxTrackedKeys;
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
        if (attempts.size() <= purgeThreshold) {
            return;
        }
        var stale = now.minus(Duration.ofHours(1));
        attempts.entrySet().removeIf(entry -> lastAttempt(entry).isBefore(stale));
        if (attempts.size() <= maxTrackedKeys) {
            return;
        }
        // Down to 80 % of the ceiling at once: a full table is not sorted again on every request.
        var excess = attempts.size() - maxTrackedKeys * 4 / 5;
        // Snapshot first: other requests keep updating the counters while this sorts.
        attempts.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), lastAttempt(entry)))
                .toList()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(excess)
                .forEach(entry -> attempts.remove(entry.getKey()));
    }

    private static Instant lastAttempt(Map.Entry<String, Deque<Instant>> entry) {
        var last = entry.getValue().peekLast();
        return last == null ? Instant.MIN : last;
    }
}
