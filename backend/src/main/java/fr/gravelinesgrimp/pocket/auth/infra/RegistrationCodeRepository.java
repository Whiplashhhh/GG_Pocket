package fr.gravelinesgrimp.pocket.auth.infra;

import fr.gravelinesgrimp.pocket.auth.domain.RegistrationCode;
import fr.gravelinesgrimp.pocket.member.domain.Role;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistrationCodeRepository extends JpaRepository<RegistrationCode, UUID> {

    Optional<RegistrationCode> findByCode(String code);

    /** Locks the code until the end of the registration: its last use cannot be taken twice. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from RegistrationCode c where c.code = :code")
    Optional<RegistrationCode> lockByCode(String code);

    List<RegistrationCode> findAllByGrantedRoleOrderByCreatedAtDesc(Role grantedRole);

    List<RegistrationCode> findAllByGrantedRoleAndActiveTrue(Role grantedRole);

    boolean existsByCode(String code);
}
