package fr.gravelinesgrimp.pocket.member.domain;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

/**
 * What a person says about themselves at sign-up (D-107): licensed at the club or not and, if not,
 * who they are to the club. Only a declaration: the board decides when it validates the account.
 *
 * @param joinNote stripped, null when licensed
 */
public record LicenceDeclaration(boolean licensed, String joinNote) {

    public static final int NOTE_MIN = 10;
    public static final int NOTE_MAX = 300;

    /**
     * @throws BusinessException {@code JOIN_NOTE_REQUIRED} when not licensed and the note is
     *     missing, shorter than {@link #NOTE_MIN} or longer than {@link #NOTE_MAX} characters
     */
    public static LicenceDeclaration of(boolean licensed, String joinNote) {
        if (licensed) {
            return new LicenceDeclaration(true, null);
        }
        var note = joinNote == null ? "" : joinNote.strip();
        var length = note.codePointCount(0, note.length());
        if (length < NOTE_MIN || length > NOTE_MAX) {
            throw new BusinessException(
                    ErrorCode.JOIN_NOTE_REQUIRED, "Not licensed: say who you are to the club");
        }
        return new LicenceDeclaration(false, note);
    }
}
