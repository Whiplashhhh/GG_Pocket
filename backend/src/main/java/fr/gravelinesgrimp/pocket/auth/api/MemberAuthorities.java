package fr.gravelinesgrimp.pocket.auth.api;

import fr.gravelinesgrimp.pocket.member.domain.MemberAccess;
import fr.gravelinesgrimp.pocket.member.domain.Role;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/** The authorities of a signed-in member: their role, plus {@code VALIDATED} once validated. */
final class MemberAuthorities {

    private MemberAuthorities() {}

    static List<GrantedAuthority> of(Role role, boolean validated) {
        var roleAuthority = new SimpleGrantedAuthority(role.authority());
        return validated
                ? List.of(roleAuthority, new SimpleGrantedAuthority(MemberAccess.VALIDATED))
                : List.of(roleAuthority);
    }
}
