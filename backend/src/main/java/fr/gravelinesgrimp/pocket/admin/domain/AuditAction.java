package fr.gravelinesgrimp.pocket.admin.domain;

/** What the audit log records (spec §5). Never rename a constant: stored in the database. */
public enum AuditAction {
    MEMBER_ROLE_CHANGED,
    MEMBER_DISABLED,
    MEMBER_ENABLED,
    /** A new account accepted by the board, as a tourist or a climber (D-107). */
    MEMBER_VALIDATED,
    /** For good, by the member or by the admin (spec §14). */
    MEMBER_DELETED,
    /** A new account the board did not recognise, deleted before any validation (D-107). */
    MEMBER_REFUSED,
    PASSWORD_RESET_LINK_CREATED,
    REGISTRATION_CODE_CREATED,
    REGISTRATION_CODE_UPDATED,
    ROUTE_DISMOUNTED,
    ROUTE_REMOUNTED,
    /** By someone else than the author (moderation). */
    ROUTE_COMMENT_DELETED,
    /** By someone else than the author (moderation). */
    SESSION_COMMENT_DELETED,
    CLUB_SECTOR_CREATED,
    CLUB_SECTOR_UPDATED,
    CLUB_SECTORS_REORDERED,
    CLUB_CORD_CREATED,
    CLUB_CORD_UPDATED,
    CLUB_HOLD_COLOR_CREATED,
    CLUB_HOLD_COLOR_UPDATED
}
