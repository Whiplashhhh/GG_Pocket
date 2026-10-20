package fr.gravelinesgrimp.pocket.auth.api;

import fr.gravelinesgrimp.pocket.auth.domain.DeviceDescription;
import fr.gravelinesgrimp.pocket.auth.domain.SessionAttributes;
import fr.gravelinesgrimp.pocket.member.application.CurrentMemberView;
import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Opens the server session of a member who just proved who they are (sign-in, registration,
 * password reset). Spec §6: 180 days of inactivity when "Rester connecté" is checked, otherwise a
 * cookie that dies with the browser (DECISIONS D-048).
 */
@Component
public class SessionEstablisher {

    /** Sliding: every visit pushes it back. */
    public static final Duration REMEMBERED_INACTIVITY = Duration.ofDays(180);

    /** Without "Rester connecté": a browser left open on a shared computer still logs out. */
    public static final Duration UNREMEMBERED_INACTIVITY = Duration.ofHours(12);

    private final SecurityContextRepository securityContextRepository;
    private final Clock clock;

    public SessionEstablisher(SecurityContextRepository securityContextRepository, Clock clock) {
        this.securityContextRepository = securityContextRepository;
        this.clock = clock;
    }

    public void signIn(
            CurrentMemberView member,
            boolean remember,
            HttpServletRequest request,
            HttpServletResponse response) {
        // A brand new session for each sign-in: an id known before cannot be reused (fixation).
        var previous = request.getSession(false);
        if (previous != null) {
            previous.invalidate();
        }
        var session = request.getSession(true);
        var inactivity = remember ? REMEMBERED_INACTIVITY : UNREMEMBERED_INACTIVITY;
        session.setMaxInactiveInterval((int) inactivity.toSeconds());

        var device = DeviceDescription.fromUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
        session.setAttribute(SessionAttributes.DEVICE_ID, UUID.randomUUID().toString());
        session.setAttribute(SessionAttributes.BROWSER, device.browser().name());
        session.setAttribute(SessionAttributes.PLATFORM, device.platform().name());
        session.setAttribute(SessionAttributes.REMEMBERED, remember);
        if (remember) {
            session.setAttribute(SessionAttributes.COOKIE_REFRESHED_AT, clock.millis());
            // Read by Spring Session's cookie serializer: the cookie gets a far Max-Age (browsers
            // cap it to 400 days) instead of dying with the browser. See CurrentMemberFilter.
            request.setAttribute(SpringSessionRememberMeServices.REMEMBER_ME_LOGIN_ATTR, true);
        }

        var authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        new MemberPrincipal(member.id()),
                        null,
                        List.of(new SimpleGrantedAuthority(member.role().authority())));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

    public void signOut(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }
}
