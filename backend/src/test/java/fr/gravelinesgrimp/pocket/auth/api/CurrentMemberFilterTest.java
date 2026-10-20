package fr.gravelinesgrimp.pocket.auth.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.gravelinesgrimp.pocket.auth.domain.SessionAttributes;
import fr.gravelinesgrimp.pocket.member.application.MemberAccessService;
import fr.gravelinesgrimp.pocket.member.domain.MemberAccess;
import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;
import fr.gravelinesgrimp.pocket.member.domain.MemberStatus;
import fr.gravelinesgrimp.pocket.member.domain.Role;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.web.http.CookieSerializer;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

class CurrentMemberFilterTest {

    private static final Instant NOW = Instant.parse("2026-10-20T10:00:00Z");
    private static final UUID MEMBER = UUID.randomUUID();

    private final MemberAccessService memberAccess = mock(MemberAccessService.class);
    private final CookieSerializer cookieSerializer = mock(CookieSerializer.class);
    private final CurrentMemberFilter filter =
            new CurrentMemberFilter(
                    memberAccess, cookieSerializer, Clock.fixed(NOW, ZoneOffset.UTC));

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final MockHttpSession session = new MockHttpSession();
    private final AtomicReference<Authentication> seenByTheApp = new AtomicReference<>();

    @BeforeEach
    void signedInAsAClimber() {
        request.setSession(session);
        SecurityContextHolder.getContext()
                .setAuthentication(
                        UsernamePasswordAuthenticationToken.authenticated(
                                new MemberPrincipal(MEMBER),
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_CLIMBER"))));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void aRoleGivenSinceSignInAppliesAtOnce() throws Exception {
        memberIs(Role.BOARD, MemberStatus.ACTIVE);

        run();

        assertThat(seenByTheApp.get().getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_BOARD");
        assertThat(session.isInvalid()).isFalse();
    }

    @Test
    void aDisabledAccountLosesItsSessionImmediately() throws Exception {
        memberIs(Role.CLIMBER, MemberStatus.DISABLED);

        run();

        assertThat(seenByTheApp.get()).isNull();
        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void aDeletedAccountLosesItsSessionToo() throws Exception {
        when(memberAccess.loadAndMarkSeen(MEMBER)).thenReturn(Optional.empty());

        run();

        assertThat(seenByTheApp.get()).isNull();
        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void theLongLivedCookieIsSentAgainAfterAWeek() throws Exception {
        memberIs(Role.CLIMBER, MemberStatus.ACTIVE);
        session.setAttribute(SessionAttributes.REMEMBERED, true);
        session.setAttribute(
                SessionAttributes.COOKIE_REFRESHED_AT,
                NOW.minus(Duration.ofDays(8)).toEpochMilli());

        run();

        verify(cookieSerializer).writeCookieValue(any());
        assertThat(session.getAttribute(SessionAttributes.COOKIE_REFRESHED_AT))
                .isEqualTo(NOW.toEpochMilli());
    }

    @Test
    void theCookieIsLeftAloneWithinTheWeek() throws Exception {
        memberIs(Role.CLIMBER, MemberStatus.ACTIVE);
        session.setAttribute(SessionAttributes.REMEMBERED, true);
        session.setAttribute(
                SessionAttributes.COOKIE_REFRESHED_AT,
                NOW.minus(Duration.ofDays(6)).toEpochMilli());

        run();

        verify(cookieSerializer, never()).writeCookieValue(any());
    }

    @Test
    void aBrowserSessionCookieIsNeverMadeLongLived() throws Exception {
        memberIs(Role.CLIMBER, MemberStatus.ACTIVE);
        session.setAttribute(SessionAttributes.REMEMBERED, false);

        run();

        verify(cookieSerializer, never()).writeCookieValue(any());
    }

    private void memberIs(Role role, MemberStatus status) {
        when(memberAccess.loadAndMarkSeen(MEMBER))
                .thenReturn(Optional.of(new MemberAccess(MEMBER, role, status, null)));
    }

    private void run() throws Exception {
        filter.doFilter(
                request,
                response,
                (req, res) ->
                        seenByTheApp.set(SecurityContextHolder.getContext().getAuthentication()));
    }
}
