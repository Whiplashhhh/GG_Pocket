package fr.gravelinesgrimp.pocket.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * The single row recording which environment a database belongs to. Written once, never updated: it
 * only has a creation date.
 */
@Entity
@Table(name = "instance_marker")
public class InstanceMarker {

    public static final short SINGLETON_ID = 1;

    @Id private Short id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DeploymentEnvironment environment;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected InstanceMarker() {
        // for JPA
    }

    public InstanceMarker(DeploymentEnvironment environment, Instant createdAt) {
        this.id = SINGLETON_ID;
        this.environment = environment;
        this.createdAt = createdAt;
    }

    public DeploymentEnvironment environment() {
        return environment;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
