package fr.gravelinesgrimp.pocket.badge.domain;

import java.util.regex.Pattern;

/**
 * Reads the French grades of the club ("4a-" to "7c+", D-074) without reading the database: a "6a"
 * tier is earned by a 6a-, a 6a or a 6a+, and by nothing else (D-111).
 */
public final class GradeScale {

    private static final Pattern GRADE = Pattern.compile("([3-9])([abc])([+-]?)");

    private GradeScale() {}

    /**
     * The grade without its modifier: "6a" for "6a-", "6a" and "6a+".
     *
     * @throws IllegalArgumentException for a code that is not a French grade
     */
    public static String base(String grade) {
        var matcher = GRADE.matcher(grade);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Not a grade: " + grade);
        }
        return matcher.group(1) + matcher.group(2);
    }

    /**
     * Higher is harder.
     *
     * @throws IllegalArgumentException for a code that is not a French grade
     */
    public static int rank(String grade) {
        var matcher = GRADE.matcher(grade);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Not a grade: " + grade);
        }
        var number = matcher.group(1).charAt(0) - '0';
        var letter = matcher.group(2).charAt(0) - 'a';
        var modifier =
                switch (matcher.group(3)) {
                    case "-" -> 0;
                    case "+" -> 2;
                    default -> 1;
                };
        return number * 9 + letter * 3 + modifier;
    }
}
