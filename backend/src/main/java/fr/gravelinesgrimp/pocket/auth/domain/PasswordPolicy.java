package fr.gravelinesgrimp.pocket.auth.domain;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spec §6: at least 8 characters and not one of the most common passwords, nothing more annoying.
 * The 72-byte ceiling is a limit of bcrypt (longer passwords would be silently truncated).
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_BYTES = 72;

    private final Set<String> commonPasswords;

    public PasswordPolicy(Set<String> commonPasswords) {
        this.commonPasswords =
                commonPasswords.stream()
                        .map(password -> password.toLowerCase(Locale.ROOT))
                        .collect(Collectors.toUnmodifiableSet());
    }

    public void check(String password) {
        if (password == null || password.codePointCount(0, password.length()) < MIN_LENGTH) {
            throw new BusinessException(ErrorCode.PASSWORD_TOO_SHORT, "Password too short");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new BusinessException(ErrorCode.PASSWORD_TOO_LONG, "Password too long");
        }
        if (commonPasswords.contains(password.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(ErrorCode.PASSWORD_TOO_COMMON, "Password too common");
        }
    }
}
