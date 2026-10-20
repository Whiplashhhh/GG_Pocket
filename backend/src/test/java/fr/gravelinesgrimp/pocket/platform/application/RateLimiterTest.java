package fr.gravelinesgrimp.pocket.platform.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.gravelinesgrimp.pocket.platform.application.RateLimiter.Rule;
import fr.gravelinesgrimp.pocket.platform.domain.RateLimitedException;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

class RateLimiterTest {

    private static final Rule RULE = new Rule("test", 3, Duration.ofMinutes(15));

    private final MutableClock clock = new MutableClock(Instant.parse("2026-10-20T10:00:00Z"));
    private final RateLimiter limiter = new RateLimiter(clock);

    @Test
    void refusesTheAttemptAfterTheLimitAndSaysWhenToComeBack() {
        limiter.consume(RULE, "1.2.3.4");
        clock.advance(Duration.ofMinutes(5));
        limiter.consume(RULE, "1.2.3.4");
        limiter.consume(RULE, "1.2.3.4");

        assertThatThrownBy(() -> limiter.consume(RULE, "1.2.3.4"))
                .isInstanceOfSatisfying(
                        RateLimitedException.class,
                        e -> assertThat(e.retryAfter()).isEqualTo(Duration.ofMinutes(10)));
    }

    @Test
    void theWindowSlides() {
        for (var i = 0; i < 3; i++) {
            limiter.consume(RULE, "key");
        }
        clock.advance(Duration.ofMinutes(15));
        assertThatCode(() -> limiter.consume(RULE, "key")).doesNotThrowAnyException();
    }

    @Test
    void keysAndRulesAreIndependent() {
        for (var i = 0; i < 3; i++) {
            limiter.consume(RULE, "a");
        }
        assertThatCode(() -> limiter.consume(RULE, "b")).doesNotThrowAnyException();
        assertThatCode(() -> limiter.consume(new Rule("other", 1, Duration.ofMinutes(1)), "a"))
                .doesNotThrowAnyException();
    }

    @Test
    void failuresAreCountedSeparatelyFromChecks() {
        limiter.ensureAllowed(RULE, "marie");
        limiter.record(RULE, "marie");
        limiter.record(RULE, "marie");
        limiter.ensureAllowed(RULE, "marie");
        limiter.record(RULE, "marie");

        assertThatThrownBy(() -> limiter.ensureAllowed(RULE, "marie"))
                .isInstanceOf(RateLimitedException.class);

        limiter.reset(RULE, "marie");
        assertThatCode(() -> limiter.ensureAllowed(RULE, "marie")).doesNotThrowAnyException();
    }

    static final class MutableClock extends Clock {

        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(now, zone);
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
