package fr.gravelinesgrimp.pocket.club.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * A colour of holds, identifying routes on the wall (DESIGN.md §2). The multicolour hold keeps a
 * neutral hex: the app draws a pattern for it.
 */
@Entity
@Table(name = "hold_color")
public class HoldColor extends TimestampedEntity {

    public static final Pattern HEX = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    @Column(nullable = false, length = 30)
    private String label;

    /** {@code #RRGGBB}, upper case. */
    @Column(nullable = false, length = 7)
    private String hex;

    @Column(nullable = false)
    private boolean multicolor;

    /** An inactive colour stays on its routes but is not offered for new ones. */
    @Column(nullable = false)
    private boolean active;

    /** Display order, 1 first. */
    @Column(nullable = false)
    private int position;

    protected HoldColor() {
        // for JPA
    }

    public HoldColor(String label, String hex, boolean multicolor, boolean active, int position) {
        this.label = label.strip();
        this.hex = normalizeHex(hex);
        this.multicolor = multicolor;
        this.active = active;
        this.position = position;
    }

    public void update(String label, String hex, boolean multicolor, boolean active) {
        this.label = label.strip();
        this.hex = normalizeHex(hex);
        this.multicolor = multicolor;
        this.active = active;
    }

    /**
     * @throws IllegalArgumentException if this is not {@code #RRGGBB} (the API validates it first)
     */
    public static String normalizeHex(String hex) {
        if (hex == null || !HEX.matcher(hex).matches()) {
            throw new IllegalArgumentException("Not a #RRGGBB colour: " + hex);
        }
        return hex.toUpperCase(Locale.ROOT);
    }

    public String label() {
        return label;
    }

    public String hex() {
        return hex;
    }

    public boolean multicolor() {
        return multicolor;
    }

    public boolean active() {
        return active;
    }

    public int position() {
        return position;
    }
}
