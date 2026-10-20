package fr.gravelinesgrimp.pocket.auth.api;

import jakarta.servlet.http.HttpServletRequest;

/**
 * The address rate limits are counted against. Behind Caddy, Tomcat replaces the proxy's address
 * with the client's from {@code X-Forwarded-For}, trusting private-network proxies only ({@code
 * server.forward-headers-strategy: native}).
 */
final class ClientAddress {

    private ClientAddress() {}

    static String of(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
