package fr.gravelinesgrimp.pocket.auth.domain;

/**
 * A rough idea of the device behind a session, from its user agent, so that a member recognizes it
 * in "Mes appareils" ("Safari sur iPhone"). Never used for any security decision.
 */
public record DeviceDescription(Browser browser, Platform platform) {

    public enum Browser {
        SAFARI,
        CHROME,
        FIREFOX,
        EDGE,
        SAMSUNG_INTERNET,
        OPERA,
        OTHER
    }

    public enum Platform {
        IPHONE,
        IPAD,
        ANDROID,
        MAC,
        WINDOWS,
        LINUX,
        OTHER
    }

    public static final DeviceDescription UNKNOWN =
            new DeviceDescription(Browser.OTHER, Platform.OTHER);

    /** Order matters: Edge and Opera also announce Chrome, Chrome also announces Safari. */
    public static DeviceDescription fromUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return UNKNOWN;
        }
        return new DeviceDescription(browserOf(userAgent), platformOf(userAgent));
    }

    private static Browser browserOf(String ua) {
        if (ua.contains("Edg/") || ua.contains("EdgiOS/") || ua.contains("EdgA/")) {
            return Browser.EDGE;
        }
        if (ua.contains("OPR/") || ua.contains("OPiOS/")) {
            return Browser.OPERA;
        }
        if (ua.contains("SamsungBrowser/")) {
            return Browser.SAMSUNG_INTERNET;
        }
        if (ua.contains("Firefox/") || ua.contains("FxiOS/")) {
            return Browser.FIREFOX;
        }
        if (ua.contains("Chrome/") || ua.contains("CriOS/")) {
            return Browser.CHROME;
        }
        if (ua.contains("Safari/") || ua.contains("AppleWebKit/")) {
            return Browser.SAFARI;
        }
        return Browser.OTHER;
    }

    private static Platform platformOf(String ua) {
        if (ua.contains("iPhone") || ua.contains("iPod")) {
            return Platform.IPHONE;
        }
        if (ua.contains("iPad")) {
            return Platform.IPAD;
        }
        if (ua.contains("Android")) {
            return Platform.ANDROID;
        }
        if (ua.contains("Macintosh") || ua.contains("Mac OS X")) {
            return Platform.MAC;
        }
        if (ua.contains("Windows")) {
            return Platform.WINDOWS;
        }
        if (ua.contains("Linux") || ua.contains("CrOS")) {
            return Platform.LINUX;
        }
        return Platform.OTHER;
    }
}
