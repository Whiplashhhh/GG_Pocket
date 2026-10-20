package fr.gravelinesgrimp.pocket;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.MemberIdentity;
import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;

import jakarta.servlet.http.Cookie;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/** Members created straight in the database, and ways to act as them in MockMvc requests. */
@TestComponent
public class MemberFixtures {

    public static final String PASSWORD = "Bras de T-Rex 2026";
    public static final String SESSION_COOKIE = "GG_SESSION";
    public static final String IPHONE_SAFARI =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 26_0 like Mac OS X) AppleWebKit/605.1.15"
                    + " (KHTML, like Gecko) Version/26.0 Mobile/15E148 Safari/604.1";

    private static final AtomicInteger SEQUENCE = new AtomicInteger();
    private static final AtomicInteger ADDRESS = new AtomicInteger();

    private final MemberRepository members;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public MemberFixtures(MemberRepository members, PasswordEncoder passwordEncoder, Clock clock) {
        this.members = members;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    /** A new active member with a unique nickname and {@link #PASSWORD}. */
    public Member create(Role role) {
        var n = SEQUENCE.incrementAndGet();
        var member =
                new Member(
                        new MemberIdentity("Grimpeur" + n, "Prénom" + n, "Nom" + n, null),
                        passwordEncoder.encode(PASSWORD),
                        role,
                        LocalDate.of(1990, 1, 1),
                        false,
                        clock.instant());
        return members.saveAndFlush(member);
    }

    /** Acts as the member for one request, without a server session. */
    public static RequestPostProcessor as(Member member) {
        return authentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        new MemberPrincipal(member.id()),
                        null,
                        List.of(new SimpleGrantedAuthority(member.role().authority()))));
    }

    /**
     * A CSRF token the way the app sends it: the XSRF-TOKEN cookie, copied into the X-XSRF-TOKEN
     * header. Not Spring Security's {@code csrf()} post-processor: it swaps, for good, the token
     * repository of the filter shared by every integration test, which then stops writing cookies.
     */
    public static RequestPostProcessor xsrf() {
        var token = UUID.randomUUID().toString();
        return request -> {
            var cookie = new Cookie("XSRF-TOKEN", token);
            var existing = request.getCookies();
            var cookies = existing == null ? new Cookie[0] : existing;
            var all = Arrays.copyOf(cookies, cookies.length + 1);
            all[cookies.length] = cookie;
            request.setCookies(all);
            request.addHeader("X-XSRF-TOKEN", token);
            return request;
        };
    }

    /**
     * Each request from its own address, so the rate limits of the auth endpoints never mix up
     * unrelated tests (they share one application).
     */
    public static RequestPostProcessor freshAddress() {
        var n = ADDRESS.incrementAndGet();
        var address = "10.%d.%d.%d".formatted((n >> 16) & 0xFF, (n >> 8) & 0xFF, n & 0xFF);
        return request -> {
            request.setRemoteAddr(address);
            return request;
        };
    }

    /** Signs in through the real endpoint and returns the session cookie. */
    public static Cookie signIn(MockMvc mockMvc, Member member, boolean remember) throws Exception {
        return signIn(mockMvc, member.nickname(), PASSWORD, remember, IPHONE_SAFARI);
    }

    public static Cookie signIn(
            MockMvc mockMvc, String nickname, String password, boolean remember, String userAgent)
            throws Exception {
        var response =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .with(xsrf())
                                        .with(freshAddress())
                                        .header(HttpHeaders.USER_AGENT, userAgent)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
                                                {"nickname": "%s", "password": "%s", "rememberMe": %s}
                                                """
                                                        .formatted(nickname, password, remember)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse();
        var cookie = response.getCookie(SESSION_COOKIE);
        if (cookie == null) {
            throw new AssertionError("No session cookie after sign-in");
        }
        return cookie;
    }
}
