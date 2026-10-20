package fr.gravelinesgrimp.pocket.platform.domain;

import java.time.Duration;

/** Too many attempts: the client may try again after {@link #retryAfter()}. */
public class RateLimitedException extends BusinessException {

    private static final long serialVersionUID = 1L;

    private final Duration retryAfter;

    public RateLimitedException(String rule, Duration retryAfter) {
        super(ErrorCode.RATE_LIMITED, "Rate limit reached for " + rule);
        this.retryAfter = retryAfter;
    }

    public Duration retryAfter() {
        return retryAfter;
    }
}
