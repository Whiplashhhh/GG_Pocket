package fr.gravelinesgrimp.pocket.topo.domain;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A route set on a cord (spec §7). Grade, cord and colours are ids of the club configuration, read
 * in one go when listing routes; the sector is the one of the cord.
 *
 * <p>Status rules: {@code ACTIVE} ⇄ {@code LAST_CHANCE}, both → {@code DISMOUNTED}, and {@code
 * DISMOUNTED} → {@code ACTIVE} to undo a dismount. A dismounted route is read-only.
 */
@Entity
@Table(name = "route")
public class Route extends TimestampedEntity {

    @Column(nullable = false, length = 60)
    private String name;

    @Column(name = "grade_id", nullable = false)
    private UUID gradeId;

    @Column(name = "cord_id", nullable = false)
    private UUID cordId;

    @Column(name = "primary_color_id", nullable = false)
    private UUID primaryColorId;

    @Column(name = "secondary_color_id")
    private UUID secondaryColorId;

    @Column(name = "opened_on", nullable = false)
    private LocalDate openedOn;

    @Column(name = "setter_note", length = 60)
    private String setterNote;

    @Column(length = 500)
    private String advice;

    @Column(name = "photo_id")
    private UUID photoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RouteStatus status;

    @Column(name = "planned_dismount_on")
    private LocalDate plannedDismountOn;

    @Column(name = "dismounted_at")
    private Instant dismountedAt;

    @Column(name = "author_id", updatable = false)
    private UUID authorId;

    @ElementCollection
    @CollectionTable(name = "route_setter", joinColumns = @JoinColumn(name = "route_id"))
    @Column(name = "member_id", nullable = false)
    private Set<UUID> setterIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "route_style", joinColumns = @JoinColumn(name = "route_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "style", nullable = false, length = 16)
    private Set<RouteStyle> styles = new HashSet<>();

    protected Route() {
        // for JPA
    }

    /** A new route on the wall; the caller has checked the draft. */
    public Route(RouteDraft draft, UUID authorId) {
        this.status = RouteStatus.ACTIVE;
        this.authorId = authorId;
        apply(draft);
    }

    /**
     * @throws BusinessException {@code ROUTE_DISMOUNTED} if the route is off the wall
     */
    public void update(RouteDraft draft) {
        ensureOnTheWall();
        apply(draft);
    }

    /**
     * @throws BusinessException {@code ROUTE_DISMOUNTED} if the route is off the wall
     */
    public void changePhoto(UUID photoId) {
        ensureOnTheWall();
        this.photoId = photoId;
    }

    /**
     * Announces (or re-dates) the last chance of the route.
     *
     * @throws BusinessException {@code ROUTE_DISMOUNTED} if it is off the wall, {@code
     *     DATE_IN_THE_PAST} if the planned date is before today
     */
    public void announceLastChance(LocalDate plannedOn, LocalDate today) {
        ensureOnTheWall();
        if (plannedOn != null && plannedOn.isBefore(today)) {
            throw new BusinessException(
                    ErrorCode.DATE_IN_THE_PAST, "Dismount planned on " + plannedOn);
        }
        this.status = RouteStatus.LAST_CHANCE;
        this.plannedDismountOn = plannedOn;
    }

    /**
     * Back to a plain active route.
     *
     * @throws BusinessException {@code ROUTE_DISMOUNTED} if it is off the wall, {@code
     *     ROUTE_STATUS_CONFLICT} if it is not in last chance
     */
    public void cancelLastChance() {
        ensureOnTheWall();
        if (status != RouteStatus.LAST_CHANCE) {
            throw conflict("Not in last chance");
        }
        this.status = RouteStatus.ACTIVE;
        this.plannedDismountOn = null;
    }

    /**
     * @throws BusinessException {@code ROUTE_STATUS_CONFLICT} if it is already dismounted
     */
    public void dismount(Instant at) {
        if (status == RouteStatus.DISMOUNTED) {
            throw conflict("Already dismounted");
        }
        this.status = RouteStatus.DISMOUNTED;
        this.plannedDismountOn = null;
        this.dismountedAt = at;
    }

    /**
     * Undoes a dismount: the route is back on the wall, as an active route.
     *
     * @throws BusinessException {@code ROUTE_STATUS_CONFLICT} if it is not dismounted
     */
    public void remount() {
        if (status != RouteStatus.DISMOUNTED) {
            throw conflict("Not dismounted");
        }
        this.status = RouteStatus.ACTIVE;
        this.dismountedAt = null;
    }

    /**
     * @throws BusinessException {@code ROUTE_DISMOUNTED} if the route is off the wall (read-only)
     */
    public void ensureOnTheWall() {
        if (status == RouteStatus.DISMOUNTED) {
            throw new BusinessException(ErrorCode.ROUTE_DISMOUNTED, "Route " + id() + " is off");
        }
    }

    public boolean isDismounted() {
        return status == RouteStatus.DISMOUNTED;
    }

    private void apply(RouteDraft draft) {
        this.name = draft.name().strip();
        this.gradeId = draft.gradeId();
        this.cordId = draft.cordId();
        this.primaryColorId = draft.holdColorIds().get(0);
        this.secondaryColorId =
                draft.holdColorIds().size() > 1 ? draft.holdColorIds().get(1) : null;
        this.openedOn = draft.openedOn();
        this.setterNote = blankToNull(draft.setterNote());
        this.advice = blankToNull(draft.advice());
        // Same collection instances: Hibernate tracks their content.
        this.setterIds.clear();
        this.setterIds.addAll(draft.setterIds());
        this.styles.clear();
        this.styles.addAll(draft.styles());
    }

    private static BusinessException conflict(String message) {
        return new BusinessException(ErrorCode.ROUTE_STATUS_CONFLICT, message);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    public String name() {
        return name;
    }

    public UUID gradeId() {
        return gradeId;
    }

    public UUID cordId() {
        return cordId;
    }

    /** One or two colours, in the order chosen by the setter. */
    public List<UUID> holdColorIds() {
        var ids = new ArrayList<UUID>(2);
        ids.add(primaryColorId);
        if (secondaryColorId != null) {
            ids.add(secondaryColorId);
        }
        return List.copyOf(ids);
    }

    public LocalDate openedOn() {
        return openedOn;
    }

    public String setterNote() {
        return setterNote;
    }

    public String advice() {
        return advice;
    }

    public UUID photoId() {
        return photoId;
    }

    public RouteStatus status() {
        return status;
    }

    public LocalDate plannedDismountOn() {
        return plannedDismountOn;
    }

    public Instant dismountedAt() {
        return dismountedAt;
    }

    public UUID authorId() {
        return authorId;
    }

    public Set<UUID> setterIds() {
        return Set.copyOf(setterIds);
    }

    /** In the order of {@link RouteStyle}, whatever the order chosen. */
    public List<RouteStyle> styles() {
        return styles.stream().sorted().toList();
    }
}
