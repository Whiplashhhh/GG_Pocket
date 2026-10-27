package fr.gravelinesgrimp.pocket.social.infra;

import fr.gravelinesgrimp.pocket.social.domain.Friendship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    /** The row between two members, whoever asked. */
    @Query(
            "select f from Friendship f where (f.requesterId = :a and f.addresseeId = :b)"
                    + " or (f.requesterId = :b and f.addresseeId = :a)")
    Optional<Friendship> findBetween(UUID a, UUID b);

    @Query(
            "select count(f) > 0 from Friendship f where f.status = "
                    + " fr.gravelinesgrimp.pocket.social.domain.FriendshipStatus.ACCEPTED"
                    + " and ((f.requesterId = :a and f.addresseeId = :b)"
                    + " or (f.requesterId = :b and f.addresseeId = :a))")
    boolean areFriends(UUID a, UUID b);

    /** Every row I am part of, pending or accepted, newest first. */
    @Query(
            "select f from Friendship f where f.requesterId = :memberId or f.addresseeId = :memberId"
                    + " order by f.createdAt desc")
    List<Friendship> findAllOf(UUID memberId);
}
