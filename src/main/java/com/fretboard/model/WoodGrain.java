package com.fretboard.model;

import javafx.scene.paint.Color;

/**
 * Enum representing different wood grain types for the fretboard display.
 * Each wood type has a distinct color scheme.
 */
public enum WoodGrain {

    ROSEWOOD("Rosewood", Color.rgb(47, 30, 25), Color.rgb(62, 39, 35)),
    EBONY("Ebony", Color.rgb(25, 22, 20), Color.rgb(35, 30, 28)),
    MAPLE("Maple", Color.rgb(180, 150, 100), Color.rgb(200, 170, 120)),
    MAHOGANY("Mahogany", Color.rgb(80, 45, 30), Color.rgb(103, 58, 39)),
    WALNUT("Walnut", Color.rgb(55, 40, 30), Color.rgb(75, 55, 40));

    private final String displayName;
    private final Color primaryColor;
    private final Color secondaryColor;

    WoodGrain(String displayName, Color primaryColor, Color secondaryColor) {
        this.displayName = displayName;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }

    /**
     * Gets the display name for this wood grain type.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the primary (darker) color for this wood grain type.
     *
     * @return the primary color
     */
    public Color getPrimaryColor() {
        return primaryColor;
    }

    /**
     * Gets the secondary (lighter) color for this wood grain type.
     *
     * @return the secondary color
     */
    public Color getSecondaryColor() {
        return secondaryColor;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
