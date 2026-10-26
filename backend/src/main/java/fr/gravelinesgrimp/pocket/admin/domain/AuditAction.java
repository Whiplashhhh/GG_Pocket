package fr.gravelinesgrimp.pocket.admin.domain;

/** What the audit log records (spec §5). Never rename a constant: stored in the database. */
public enum AuditAction {
    MEMBER_ROLE_CHANGED,
    MEMBER_DISABLED,
    MEMBER_ENABLED,
    /** For good, by the member or by the admin (spec §14). */
    MEMBER_DELETED,
    PASSWORD_RESET_LINK_CREATED,
    REGISTRATION_CODE_CREATED,
    REGISTRATION_CODE_UPDATED,
    ROUTE_DISMOUNTED,
    ROUTE_REMOUNTED,
    /** By someone else than the author (moderation). */
    ROUTE_COMMENT_DELETED,
    CLUB_SECTOR_CREATED,
    CLUB_SECTOR_UPDATED,
    CLUB_SECTORS_REORDERED,
    CLUB_CORD_CREATED,
    CLUB_CORD_UPDATED,
    CLUB_HOLD_COLOR_CREATED,
    CLUB_HOLD_COLOR_UPDATED
}
