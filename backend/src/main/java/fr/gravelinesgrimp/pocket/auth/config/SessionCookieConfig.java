package fr.gravelinesgrimp.pocket.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * The session cookie (spec §6), declared here rather than through {@code server.servlet.session.*}:
 * those properties only reach Spring Session with an embedded server, so tests would otherwise get
 * another cookie than production.
 */
@Configuration(proxyBeanMethods = false)
class SessionCookieConfig {

    /**
     * "__Host-": the browser only accepts it from this very host, over HTTPS, for the whole site; a
     * sibling subdomain (the dev environment) cannot plant one.
     */
    static final String COOKIE_NAME = "__Host-GG_SESSION";

    @Bean
    CookieSerializer cookieSerializer() {
        var serializer = new DefaultCookieSerializer();
        serializer.setCookieName(COOKIE_NAME);
        serializer.setCookiePath("/");
        // Set by the server, unreadable by scripts: Safari caps script-written cookies to 7 days.
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseSecureCookie(true);
        serializer.setSameSite("Lax");
        // "Rester connecté": the sign-in sets this request attribute, the cookie then outlives
        // the browser (see SessionEstablisher and CurrentMemberFilter).
        serializer.setRememberMeRequestAttribute(
                SpringSessionRememberMeServices.REMEMBER_ME_LOGIN_ATTR);
        return serializer;
    }
}
