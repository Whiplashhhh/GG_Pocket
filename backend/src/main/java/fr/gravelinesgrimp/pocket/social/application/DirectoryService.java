package fr.gravelinesgrimp.pocket.social.application;

import fr.gravelinesgrimp.pocket.member.application.ValidatedMember;
import fr.gravelinesgrimp.pocket.member.domain.Age;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.social.domain.Friendship;
import fr.gravelinesgrimp.pocket.social.domain.FriendshipState;
import fr.gravelinesgrimp.pocket.social.infra.FriendshipRepository;
import fr.gravelinesgrimp.pocket.topo.application.FavoriteService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The directory and the public profiles (spec §8.2, §8.8, §14): what every member may see of the
 * others. Only active members are listed; a disabled or former member's profile is "not found".
 */
@Service
public class DirectoryService {

    private final MemberRepository members;
    private final FriendshipRepository friendships;
    private final Friendships friendshipStates;
    private final FavoriteService favorites;
    private final Clock clock;

    public DirectoryService(
            MemberRepository members,
            FriendshipRepository friendships,
            Friendships friendshipStates,
            FavoriteService favorites,
            Clock clock) {
        this.members = members;
        this.friendships = friendships;
        this.friendshipStates = friendshipStates;
        this.favorites = favorites;
        this.clock = clock;
    }

    /** By last name then first name. At most a couple hundred members: no paging. */
    @ValidatedMember
    @Transactional(readOnly = true)
    public DirectoryView directory(UUID me) {
        Map<UUID, Friendship> mine =
                friendships.findAllOf(me).stream()
                        .collect(Collectors.toMap(f -> f.otherThan(me), Function.identity()));
        var today = ClubTime.today(clock);
        var active =
                members.findAllForDirectory().stream().filter(Member::isVisibleInClub).toList();
        var entries =
                active.stream()
                        .filter(member -> !member.id().equals(me))
                        .map(
                                member ->
                                        new DirectoryView.EntryView(
                                                MemberCardView.of(member, today),
                                                FriendshipState.of(mine.get(member.id()), me)))
                        .toList();
        return new DirectoryView(active.size(), entries);
    }

    /**
     * @throws BusinessException {@code NOT_FOUND} (unknown, disabled or former member)
     */
    @ValidatedMember
    @Transactional(readOnly = true)
    public MemberProfileView profile(UUID me, UUID memberId) {
        var member =
                members.findById(memberId)
                        .filter(Member::isVisibleInClub)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No member"));
        var age =
                member.showAge() && member.birthDate() != null
                        ? Age.yearsOn(member.birthDate(), ClubTime.today(clock))
                        : null;
        return new MemberProfileView(
                MemberCardView.of(member, ClubTime.today(clock)),
                age,
                member.climbingSinceYear(),
                member.bio(),
                member.favoriteRouteId(),
                favorites.listOf(memberId, me),
                friendshipStates.between(me, memberId));
    }
}
