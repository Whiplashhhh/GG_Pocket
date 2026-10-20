package fr.gravelinesgrimp.pocket.member.domain;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * The nickname is the login: 3 to 20 letters without accents, digits, dots, underscores or dashes
 * (DECISIONS D-046). Unique whatever the case; displayed as typed.
 */
public final class Nickname {

    public static final String PATTERN = "^[A-Za-z0-9._-]{3,20}$";

    private static final Pattern COMPILED = Pattern.compile(PATTERN);

    private Nickname() {}

    public static boolean isValid(String nickname) {
        return nickname != null && COMPILED.matcher(nickname).matches();
    }

    /** Form used to compare nicknames: "Marie" and "marie" are the same login. */
    public static String key(String nickname) {
        return nickname.strip().toLowerCase(Locale.ROOT);
    }
}
