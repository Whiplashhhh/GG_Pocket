package fr.gravelinesgrimp.pocket.member.application;

import fr.gravelinesgrimp.pocket.member.domain.MemberAccess;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/** Reads the role and status of the signed-in member on every request. */
@Service
public class MemberAccessService {

    /** "Last activity" in the member table only needs to be roughly right. */
    static final Duration LAST_SEEN_PRECISION = Duration.ofHours(1);

    private final MemberRepository members;
    private final Clock clock;

    public MemberAccessService(MemberRepository members, Clock clock) {
        this.members = members;
        this.clock = clock;
    }

    /** The member if they still exist; also records their activity, at most once an hour. */
    @Transactional
    public Optional<MemberAccess> loadAndMarkSeen(UUID memberId) {
        var access = members.findAccess(memberId);
        access.filter(MemberAccess::isActive)
                .filter(
                        member ->
                                member.lastSeenAt() == null
                                        || member.lastSeenAt()
                                                .isBefore(
                                                        clock.instant().minus(LAST_SEEN_PRECISION)))
                .ifPresent(member -> members.markSeen(member.id(), clock.instant()));
        return access;
    }
}
