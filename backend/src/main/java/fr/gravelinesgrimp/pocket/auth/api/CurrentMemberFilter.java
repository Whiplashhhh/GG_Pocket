package fr.gravelinesgrimp.pocket.auth.api;

import fr.gravelinesgrimp.pocket.auth.domain.SessionAttributes;
import fr.gravelinesgrimp.pocket.member.application.MemberAccessService;
import fr.gravelinesgrimp.pocket.member.domain.MemberAccess;
import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;

/**
 * Runs on every request of a signed-in member, before authorization:
 *
 * <ul>
 *   <li>reads the member's role and status again (one small query): a new role applies at once, and
 *       a disabled or deleted account loses its session immediately (spec §6);
 *   <li>sends the long-lived session cookie again now and then, so its browser-side expiry slides
 *       like the server-side one (DECISIONS D-048).
 * </ul>
 *
 * <p>Not a Spring bean: it would otherwise also be registered as a plain servlet filter.
 */
public class CurrentMemberFilter extends OncePerRequestFilter {

    static final Duration COOKIE_REFRESH_INTERVAL = Duration.ofDays(7);

    private final MemberAccessService memberAccess;
    private final CookieSerializer cookieSerializer;
    private final Clock clock;

    public CurrentMemberFilter(
            MemberAccessService memberAccess, CookieSerializer cookieSerializer, Clock clock) {
        this.memberAccess = memberAccess;
        this.cookieSerializer = cookieSerializer;
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof MemberPrincipal me) {
            var access = memberAccess.loadAndMarkSeen(me.memberId()).filter(MemberAccess::isActive);
            if (access.isEmpty()) {
                var session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                SecurityContextHolder.clearContext();
            } else {
                refreshAuthorities(me, access.get());
                refreshLongLivedCookie(request, response);
            }
        }
        chain.doFilter(request, response);
    }

    /** For this request only; the session keeps just the member id that matters. */
    private static void refreshAuthorities(MemberPrincipal me, MemberAccess access) {
        var fresh =
                UsernamePasswordAuthenticationToken.authenticated(
                        me, null, MemberAuthorities.of(access.role(), access.validated()));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(fresh);
        SecurityContextHolder.setContext(context);
    }

    private void refreshLongLivedCookie(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null
                || !Boolean.TRUE.equals(session.getAttribute(SessionAttributes.REMEMBERED))) {
            return;
        }
        var refreshedAt = session.getAttribute(SessionAttributes.COOKIE_REFRESHED_AT);
        var now = clock.millis();
        if (refreshedAt instanceof Long last && now - last < COOKIE_REFRESH_INTERVAL.toMillis()) {
            return;
        }
        request.setAttribute(SpringSessionRememberMeServices.REMEMBER_ME_LOGIN_ATTR, true);
        cookieSerializer.writeCookieValue(
                new CookieSerializer.CookieValue(request, response, session.getId()));
        session.setAttribute(SessionAttributes.COOKIE_REFRESHED_AT, now);
    }
}
