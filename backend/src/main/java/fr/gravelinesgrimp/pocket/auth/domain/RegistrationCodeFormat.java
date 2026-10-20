package fr.gravelinesgrimp.pocket.auth.domain;

import java.util.Locale;
import java.util.random.RandomGenerator;

/**
 * Registration codes are 8 characters from an alphabet without look-alikes (no 0/O, no 1/I),
 * displayed as {@code K7XM-P2QA}. Typing is forgiving: case, spaces and dashes do not matter.
 */
public final class RegistrationCodeFormat {

    static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    static final int LENGTH = 8;

    private RegistrationCodeFormat() {}

    /** 32^8 ≈ 10^12 possible codes: guessing one with rate-limited attempts is hopeless. */
    public static String generate(RandomGenerator random) {
        var code = new StringBuilder(LENGTH);
        for (var i = 0; i < LENGTH; i++) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }

    /** The stored form of what someone typed: upper case, without spaces nor dashes. */
    public static String normalize(String typed) {
        if (typed == null) {
            return "";
        }
        return typed.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    public static String display(String code) {
        return code.length() == LENGTH ? code.substring(0, 4) + "-" + code.substring(4) : code;
    }
}
