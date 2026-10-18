package fr.gravelinesgrimp.pocket.platform.config;

import fr.gravelinesgrimp.pocket.platform.api.error.ProblemDetailsSecurityErrorHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Closed by default: only the endpoints listed here are public, everything else requires a logged
 * in member. Authorization rules beyond "logged in" live in the services (method security).
 */
@Configuration(proxyBeanMethods = false)
class SecurityConfig {

    @Bean
    SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http, ProblemDetailsSecurityErrorHandler errorHandler) throws Exception {
        http.authorizeHttpRequests(
                        authorize ->
                                authorize
                                        .requestMatchers(HttpMethod.GET, "/api/v1/meta")
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
                                        .anyRequest()
                                        .authenticated())
                // Single-page app: token in a cookie readable by the front, sent back in a header.
                .csrf(csrf -> csrf.spa())
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
}
