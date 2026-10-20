package fr.gravelinesgrimp.pocket.admin.domain;

/** What the audit log records (spec §5). Never rename a constant: stored in the database. */
public enum AuditAction {
    MEMBER_ROLE_CHANGED,
    MEMBER_DISABLED,
    MEMBER_ENABLED,
    PASSWORD_RESET_LINK_CREATED,
    REGISTRATION_CODE_CREATED,
    REGISTRATION_CODE_UPDATED
}
