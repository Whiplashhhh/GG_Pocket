package fr.gravelinesgrimp.pocket.badge.application;

import fr.gravelinesgrimp.pocket.badge.infra.BadgeAwardRepository;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** The badge library of a profile (spec §8.8): public, like the rest of the profile. */
@Service
public class BadgeService {

    private final BadgeAwardRepository awards;
    private final MemberRepository members;

    public BadgeService(BadgeAwardRepository awards, MemberRepository members) {
        this.awards = awards;
        this.members = members;
    }

    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional(readOnly = true)
    public BadgeLibraryView mine(UUID memberId) {
        return BadgeLibraryView.of(awards.countPerCode(memberId));
    }

    /**
     * @throws BusinessException {@code NOT_FOUND} (unknown, disabled or former member)
     */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional(readOnly = true)
    public BadgeLibraryView of(UUID memberId) {
        members.findById(memberId)
                .filter(Member::isActive)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No member"));
        return BadgeLibraryView.of(awards.countPerCode(memberId));
    }
}
