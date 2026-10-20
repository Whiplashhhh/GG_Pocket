package fr.gravelinesgrimp.pocket.member.domain;

import org.springframework.security.core.AuthenticatedPrincipal;

import java.io.Serializable;
import java.util.UUID;

/**
 * Who is signed in, as stored in the server session. Only the member id: role and status are read
 * again from the database on every request, so a role change or a disabled account takes effect
 * immediately. Its name (the member id, not the nickname, which can change) indexes the sessions of
 * a member.
 */
public record MemberPrincipal(UUID memberId) implements AuthenticatedPrincipal, Serializable {

    @Override
    public String getName() {
        return memberId.toString();
    }
}
