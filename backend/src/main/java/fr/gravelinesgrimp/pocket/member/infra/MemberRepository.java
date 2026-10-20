package fr.gravelinesgrimp.pocket.member.infra;

import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.MemberAccess;
import fr.gravelinesgrimp.pocket.member.domain.MemberStatus;
import fr.gravelinesgrimp.pocket.member.domain.Role;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {

    /** Login lookup, whatever the case typed (index {@code member_nickname_key}). */
    @Query("select m from Member m where lower(m.nickname) = lower(:nickname)")
    Optional<Member> findByNicknameIgnoreCase(String nickname);

    @Query(
            "select case when count(m) > 0 then true else false end from Member m"
                    + " where lower(m.nickname) = lower(:nickname)"
                    + " and m.id <> :excludedId")
    boolean nicknameTakenByAnother(String nickname, UUID excludedId);

    @Query(
            "select case when count(m) > 0 then true else false end from Member m"
                    + " where lower(m.email) = lower(:email)"
                    + " and m.id <> :excludedId")
    boolean emailTakenByAnother(String email, UUID excludedId);

    /** Read on every authenticated request: keep it to a handful of columns. */
    @Query(
            "select new fr.gravelinesgrimp.pocket.member.domain.MemberAccess("
                    + "m.id, m.role, m.status, m.lastSeenAt) from Member m where m.id = :id")
    Optional<MemberAccess> findAccess(UUID id);

    @Modifying
    @Query("update Member m set m.lastSeenAt = :now where m.id = :id")
    void markSeen(UUID id, Instant now);

    /**
     * Locks the active admins until the end of the transaction, so that two admins demoting each
     * other at the same time cannot leave the club without any.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.role = :role and m.status = :status")
    List<Member> lockAllByRoleAndStatus(Role role, MemberStatus status);

    @Query("select m from Member m order by lower(m.lastName), lower(m.firstName)")
    List<Member> findAllForDirectory();
}
