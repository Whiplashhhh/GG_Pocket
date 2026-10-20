package fr.gravelinesgrimp.pocket.auth.infra;

import fr.gravelinesgrimp.pocket.auth.domain.PasswordResetToken;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /** Locks the link while it is being used: two tabs cannot both use it. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from PasswordResetToken t where t.tokenHash = :tokenHash")
    Optional<PasswordResetToken> lockByTokenHash(String tokenHash);

    /** A new link replaces the previous ones: only the latest can be used. */
    @Modifying
    @Query("delete from PasswordResetToken t where t.memberId = :memberId and t.usedAt is null")
    void deleteUnusedOf(UUID memberId);
}
