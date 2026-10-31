package fr.gravelinesgrimp.pocket.badge.application;

import fr.gravelinesgrimp.pocket.badge.domain.Badge;
import fr.gravelinesgrimp.pocket.badge.domain.BadgeCode;
import fr.gravelinesgrimp.pocket.badge.domain.BadgeCount;
import fr.gravelinesgrimp.pocket.badge.domain.BadgeProgress;
import fr.gravelinesgrimp.pocket.badge.domain.FavoriteBadge;
import fr.gravelinesgrimp.pocket.badge.infra.BadgeAwardRepository;
import fr.gravelinesgrimp.pocket.badge.infra.BadgeHistoryRepository;
import fr.gravelinesgrimp.pocket.badge.infra.FavoriteBadgeRepository;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The badge library of a profile (spec §8.8): public, like the rest of the profile, but the
 * progress to the next levels is mine only. My favourite badges (D-106).
 */
@Service
public class BadgeService {

    private final BadgeAwardRepository awards;
    private final BadgeHistoryRepository history;
    private final FavoriteBadgeRepository favorites;
    private final MemberRepository members;

    public BadgeService(
            BadgeAwardRepository awards,
            BadgeHistoryRepository history,
            FavoriteBadgeRepository favorites,
            MemberRepository members) {
        this.awards = awards;
        this.history = history;
        this.favorites = favorites;
        this.members = members;
    }

    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional(readOnly = true)
    public BadgeLibraryView mine(UUID memberId) {
        var progress = BadgeProgress.of(BadgeAwards.records(history.historyOf(memberId)));
        return BadgeLibraryView.of(awards.countPerCode(memberId), favoritesOf(memberId), progress);
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
        return BadgeLibraryView.of(awards.countPerCode(memberId), favoritesOf(memberId), null);
    }

    /**
     * Replaces my favourite badges, in this order.
     *
     * @throws BusinessException {@code VALIDATION_FAILED} (more than {@link FavoriteBadge#MAX}, the
     *     same one twice, or one I have not earned)
     */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional
    public BadgeLibraryView chooseFavorites(UUID memberId, List<Badge> chosen) {
        if (chosen.size() > FavoriteBadge.MAX || new HashSet<>(chosen).size() != chosen.size()) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED, "At most 3 different favourite badges");
        }
        var earned = awards.countPerCode(memberId);
        Set<BadgeCode> earnedCodes =
                earned.stream().map(BadgeCount::code).collect(Collectors.toSet());
        if (!chosen.stream()
                .allMatch(badge -> badge.codes().stream().anyMatch(earnedCodes::contains))) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED, "A favourite badge must be earned");
        }
        favorites.deleteAllOf(memberId);
        for (var slot = 0; slot < chosen.size(); slot++) {
            favorites.save(new FavoriteBadge(memberId, chosen.get(slot), slot));
        }
        var progress = BadgeProgress.of(BadgeAwards.records(history.historyOf(memberId)));
        return BadgeLibraryView.of(earned, List.copyOf(chosen), progress);
    }

    private List<Badge> favoritesOf(UUID memberId) {
        return favorites.findAllByMemberIdOrderBySlot(memberId).stream()
                .map(FavoriteBadge::badge)
                .toList();
    }
}
