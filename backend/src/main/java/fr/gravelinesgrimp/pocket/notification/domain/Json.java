package fr.gravelinesgrimp.pocket.notification.domain;

/** Just enough JSON for a push message and a VAPID token: strings, escaped. */
public final class Json {

    private Json() {}

    /** A JSON string literal, quotes included; {@code null} gives {@code null}. */
    public static String string(String value) {
        if (value == null) {
            return "null";
        }
        var out = new StringBuilder(value.length() + 2).append('"');
        for (var i = 0; i < value.length(); i++) {
            var c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20 || c == ' ' || c == ' ') {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}
