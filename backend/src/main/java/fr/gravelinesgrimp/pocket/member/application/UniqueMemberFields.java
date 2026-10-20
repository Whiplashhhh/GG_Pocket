package fr.gravelinesgrimp.pocket.member.application;

import fr.gravelinesgrimp.pocket.member.domain.Nickname;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Nickname and email are unique among members. Checked before writing, for a precise error; the
 * database indexes remain the real guarantee when two people pick the same nickname at once.
 */
@Component
public class UniqueMemberFields {

    /** Id that matches no member, for checks made before a member exists. */
    public static final UUID NOBODY = new UUID(0, 0);

    private final MemberRepository members;

    public UniqueMemberFields(MemberRepository members) {
        this.members = members;
    }

    public void check(String nickname, String email, UUID memberId) {
        if (members.nicknameTakenByAnother(Nickname.key(nickname), memberId)) {
            throw new BusinessException(ErrorCode.NICKNAME_TAKEN, "Nickname already used");
        }
        if (email != null
                && !email.isBlank()
                && members.emailTakenByAnother(email.strip(), memberId)) {
            throw new BusinessException(ErrorCode.EMAIL_TAKEN, "Email already used");
        }
    }

    /** Turns a unique index violation into the matching business error. */
    public static RuntimeException translate(DataIntegrityViolationException e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation
                    && violation.getConstraintName() != null) {
                var name = violation.getConstraintName();
                if (name.contains("member_nickname_key")) {
                    return new BusinessException(ErrorCode.NICKNAME_TAKEN, "Nickname already used");
                }
                if (name.contains("member_email_key")) {
                    return new BusinessException(ErrorCode.EMAIL_TAKEN, "Email already used");
                }
            }
            cause = cause.getCause();
        }
        return e;
    }
}
