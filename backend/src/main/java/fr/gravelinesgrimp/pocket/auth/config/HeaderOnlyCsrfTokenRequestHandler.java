package fr.gravelinesgrimp.pocket.auth.config;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Reads the CSRF token from the {@code X-XSRF-TOKEN} header only, never from a form parameter. The
 * app always sends the header (see frontend/src/api/csrf.ts); the token is never rendered in a
 * page, so the BREACH masking of {@code csrf.spa()} has nothing to protect here.
 */
final class HeaderOnlyCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        return request.getHeader(csrfToken.getHeaderName());
    }
}
