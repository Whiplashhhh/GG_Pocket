package fr.gravelinesgrimp.pocket.session.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A visit to the gym ("séance", spec §7) and its ascents, in the order the climber gave. The date
 * rules live in {@link SessionCalendar}, the others in {@link SessionDraft}: check before applying.
 */
@Entity
@Table(name = "climbing_session")
public class ClimbingSession extends TimestampedEntity {

    @Column(name = "member_id", nullable = false, updatable = false)
    private UUID memberId;

    @Column(name = "climbed_on", nullable = false)
    private LocalDate climbedOn;

    private Integer feeling;

    @Column(length = 500)
    private String comment;

    @Column(name = "photo_id")
    private UUID photoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SessionVisibility visibility;

    @Column(name = "client_ref", nullable = false, updatable = false)
    private UUID clientRef;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position")
    private List<Ascent> ascents = new ArrayList<>();

    protected ClimbingSession() {
        // for JPA
    }

    public ClimbingSession(UUID memberId, UUID clientRef, SessionDraft draft) {
        this.memberId = memberId;
        this.clientRef = clientRef;
        apply(draft);
    }

    /**
     * Replaces everything the climber typed. Ascents are rewritten in place, position by position,
     * so that positions stay unique at every step of the flush.
     */
    public void update(SessionDraft draft) {
        apply(draft);
    }

    public void changePhoto(UUID photoId) {
        this.photoId = photoId;
    }

    private void apply(SessionDraft draft) {
        this.climbedOn = draft.climbedOn();
        this.feeling = draft.feeling();
        this.comment = draft.comment();
        this.visibility = draft.visibility();
        var wanted = draft.ascents();
        for (int position = 0; position < wanted.size(); position++) {
            if (position < ascents.size()) {
                ascents.get(position).update(wanted.get(position));
            } else {
                ascents.add(new Ascent(this, position, wanted.get(position)));
            }
        }
        while (ascents.size() > wanted.size()) {
            ascents.removeLast();
        }
    }

    public UUID memberId() {
        return memberId;
    }

    public LocalDate climbedOn() {
        return climbedOn;
    }

    public Integer feeling() {
        return feeling;
    }

    public String comment() {
        return comment;
    }

    public UUID photoId() {
        return photoId;
    }

    public SessionVisibility visibility() {
        return visibility;
    }

    public UUID clientRef() {
        return clientRef;
    }

    public List<Ascent> ascents() {
        return Collections.unmodifiableList(ascents);
    }
}
