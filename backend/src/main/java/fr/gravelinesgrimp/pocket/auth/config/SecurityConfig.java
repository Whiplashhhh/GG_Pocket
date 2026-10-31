package fr.gravelinesgrimp.pocket.auth.config;

import fr.gravelinesgrimp.pocket.auth.api.CurrentMemberFilter;
import fr.gravelinesgrimp.pocket.member.application.MemberAccessService;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.platform.api.error.ProblemDetailsSecurityErrorHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.session.web.http.CookieSerializer;

import java.time.Clock;

/**
 * Closed by default: only the endpoints listed here are public, everything else requires a signed
 * in member. Finer rules (who may manage whom) live in the services, with method security and the
 * role matrix of spec §5.
 */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
class SecurityConfig {

    /** Public writes: signing in, joining, resetting a password (all rate limited). */
    private static final String[] PUBLIC_POSTS = {
        "/api/v1/auth/login",
        "/api/v1/auth/logout",
        "/api/v1/auth/registration-code/check",
        "/api/v1/auth/register",
        "/api/v1/auth/password-reset/check",
        "/api/v1/auth/password-reset",
    };

    @Bean
    SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http,
            ProblemDetailsSecurityErrorHandler errorHandler,
            SecurityContextRepository securityContextRepository,
            MemberAccessService memberAccess,
            CookieSerializer cookieSerializer,
            Clock clock)
            throws Exception {
        http.authorizeHttpRequests(
                        authorize ->
                                authorize
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/v1/meta",
                                                "/api/v1/auth/state")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, PUBLIC_POSTS)
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/actuator/health",
                                                "/actuator/health/**")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET, "/v3/api-docs", "/v3/api-docs/**")
                                        .permitAll()
                                        // Container-level errors (rejected URL, bad request) are
                                        // re-dispatched here: keep their real status, not a 401.
                                        .requestMatchers("/error")
                                        .permitAll()
                                        // Second line of defence; the services check each action.
                                        .requestMatchers(
                                                "/api/v1/admin/registration-codes/**",
                                                "/api/v1/admin/club/**")
                                        .hasRole(Role.ADMIN.name())
                                        .requestMatchers("/api/v1/admin/**")
                                        .hasRole(Role.BOARD.name())
                                        .anyRequest()
                                        .authenticated())
                .securityContext(
                        context -> context.securityContextRepository(securityContextRepository))
                // Single-page app: token in a cookie readable by the front, sent back in a header.
                // Only the header counts: a sibling subdomain can plant a cookie, but cannot send
                // a custom header to this origin (no CORS), so no forged form gets through.
                .csrf(
                        csrf ->
                                csrf.spa()
                                        .csrfTokenRequestHandler(
                                                new HeaderOnlyCsrfTokenRequestHandler()))
                .addFilterBefore(
                        new CurrentMemberFilter(memberAccess, cookieSerializer, clock),
                        AuthorizationFilter.class)
                .exceptionHandling(
                        exceptions ->
                                exceptions
                                        .authenticationEntryPoint(errorHandler)
                                        .accessDeniedHandler(errorHandler))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /** The signed-in member lives in the server session (Spring Session JDBC), nowhere else. */
    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /** Spec §5: each role has the rights of the roles below it. */
    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role(Role.ADMIN.name())
                .implies(Role.BOARD.name())
                .role(Role.BOARD.name())
                .implies(Role.SETTER.name())
                .role(Role.SETTER.name())
                .implies(Role.CLIMBER.name())
                .role(Role.CLIMBER.name())
                .implies(Role.TOURIST.name())
                .build();
    }

    /** bcrypt today, upgradable later without forcing anyone to change their password. */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
