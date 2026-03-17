package com.example.New_Project.enums;

/**
 * Enum representing the operational status of a theatre.
 * Used to track whether a theatre is currently active or inactive.
 */
public enum TheatreStatus {
    /**
     * Theatre is operational and accepting bookings
     */
    ACTIVE("Active"),

    /**
     * Theatre is temporarily closed or inactive
     */
    INACTIVE("Inactive"),

    /**
     * Theatre is permanently closed or being decommissioned
     */
    CLOSED("Closed");

    private final String displayName;

    TheatreStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Safely parse a string to TheatreStatus
     * @param value the string representation
     * @return TheatreStatus or INACTIVE as default
     */
    public static TheatreStatus fromString(String value) {
        if (value == null || value.isEmpty()) {
            return INACTIVE;
        }
        try {
            return TheatreStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return INACTIVE;
        }
    }
}

