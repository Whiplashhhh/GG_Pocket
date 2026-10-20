package fr.gravelinesgrimp.pocket.auth.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ClientAddressTest {

    @Test
    void anIpv4AddressIsItsOwnKey() {
        assertThat(ClientAddress.key("192.0.2.10")).isEqualTo("192.0.2.10");
    }

    @Test
    void ipv6AddressesOfOneNetworkShareTheirBudget() {
        assertThat(ClientAddress.key("2001:db8:1:2:aaaa::1"))
                .isEqualTo(ClientAddress.key("2001:db8:1:2:ffff:1:2:3"))
                .isEqualTo("20010db800010002::/64");
        assertThat(ClientAddress.key("2001:db8:1:3::1"))
                .isNotEqualTo(ClientAddress.key("2001:db8:1:2::1"));
    }
}
