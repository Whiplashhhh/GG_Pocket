package fr.gravelinesgrimp.pocket.member.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.Locale;
import java.util.UUID;

/**
 * An uploaded file, already cleaned (re-encoded, without metadata). The bytes live on the media
 * volume under {@link #storageKey()}; they are only served after an access check, never from a
 * public URL.
 */
@Entity
@Table(name = "media_file")
public class MediaFile extends TimestampedEntity {

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 16)
    private MediaUsage usage;

    @Column(name = "content_type", nullable = false, updatable = false, length = 64)
    private String contentType;

    @Column(nullable = false, updatable = false)
    private int width;

    @Column(nullable = false, updatable = false)
    private int height;

    @Column(name = "size_bytes", nullable = false, updatable = false)
    private int sizeBytes;

    @Column(name = "storage_key", nullable = false, updatable = false, length = 200)
    private String storageKey;

    protected MediaFile() {
        // for JPA
    }

    public MediaFile(
            UUID ownerId,
            MediaUsage usage,
            String contentType,
            int width,
            int height,
            int sizeBytes,
            String extension) {
        this.ownerId = ownerId;
        this.usage = usage;
        this.contentType = contentType;
        this.width = width;
        this.height = height;
        this.sizeBytes = sizeBytes;
        // Its own random name: never derived from the member or the upload.
        this.storageKey =
                usage.name().toLowerCase(Locale.ROOT) + "/" + UUID.randomUUID() + "." + extension;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public MediaUsage usage() {
        return usage;
    }

    public String contentType() {
        return contentType;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int sizeBytes() {
        return sizeBytes;
    }

    public String storageKey() {
        return storageKey;
    }
}
