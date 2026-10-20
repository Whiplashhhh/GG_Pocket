package fr.gravelinesgrimp.pocket.auth.api;

import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HexFormat;

/**
 * The address rate limits are counted against. Behind Caddy, Tomcat replaces the proxy's address
 * with the client's from {@code X-Forwarded-For}, trusting private-network proxies only ({@code
 * server.forward-headers-strategy: native}).
 */
final class ClientAddress {

    private static final int IPV6_NETWORK_BYTES = 8;

    private ClientAddress() {}

    static String of(HttpServletRequest request) {
        return key(request.getRemoteAddr());
    }

    /**
     * IPv4 as is. IPv6 by its /64 network: one connection usually owns a whole /64, so counting
     * single addresses would give one person billions of budgets.
     */
    static String key(String address) {
        if (address == null || !address.contains(":")) {
            return address;
        }
        try {
            // A literal address: parsed, never looked up in the DNS.
            var bytes = InetAddress.getByName(address).getAddress();
            if (bytes.length < IPV6_NETWORK_BYTES) {
                return address;
            }
            return HexFormat.of().formatHex(bytes, 0, IPV6_NETWORK_BYTES) + "::/64";
        } catch (UnknownHostException e) {
            return address;
        }
    }
}
