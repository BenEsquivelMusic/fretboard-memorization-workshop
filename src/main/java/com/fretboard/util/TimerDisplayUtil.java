package com.fretboard.util;

import javafx.scene.paint.Color;

/**
 * Utility class for timer display formatting and color coding.
 * Consolidates duplicate timer logic from multiple training modules.
 */
public final class TimerDisplayUtil {

    /**
     * Color for normal timer display (white).
     */
    public static final Color COLOR_NORMAL = Color.WHITE;

    /**
     * Color for warning timer display (yellow/orange) when approaching target time.
     */
    public static final Color COLOR_WARNING = Color.web("#f59e0b");

    /**
     * Color for over-target timer display (red) when target time is exceeded.
     */
    public static final Color COLOR_OVER_TARGET = Color.web("#ef4444");

    /**
     * Color for success display (green).
     */
    public static final Color COLOR_SUCCESS = Color.web("#22c55e");

    private TimerDisplayUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Formats elapsed time in nanoseconds to a display string in MM:SS.t format.
     *
     * @param elapsedNanos the elapsed time in nanoseconds
     * @return a formatted string like "00:15.3"
     */
    public static String formatTime(long elapsedNanos) {
        long elapsedMillis = elapsedNanos / 1_000_000;
        long seconds = elapsedMillis / 1000;
        long tenths = (elapsedMillis % 1000) / 100;

        return String.format("%02d:%02d.%d", seconds / 60, seconds % 60, tenths);
    }

    /**
     * Formats elapsed time in seconds to a display string in MM:SS.t format.
     *
     * @param elapsedSeconds the elapsed time in seconds
     * @return a formatted string like "00:15.3"
     */
    public static String formatTimeFromSeconds(double elapsedSeconds) {
        long totalMillis = (long) (elapsedSeconds * 1000);
        long seconds = totalMillis / 1000;
        long tenths = (totalMillis % 1000) / 100;

        return String.format("%02d:%02d.%d", seconds / 60, seconds % 60, tenths);
    }

    /**
     * Gets the elapsed time in seconds from nanoseconds.
     *
     * @param elapsedNanos the elapsed time in nanoseconds
     * @return the elapsed time in seconds
     */
    public static double getElapsedSeconds(long elapsedNanos) {
        return elapsedNanos / 1_000_000_000.0;
    }

    /**
     * Gets the appropriate color for the timer based on elapsed time and target time.
     * Uses a default warning buffer of 3 seconds before the target.
     *
     * @param elapsedNanos the elapsed time in nanoseconds
     * @param targetTimeSeconds the target time in seconds
     * @return the appropriate Color for the timer display
     */
    public static Color getTimerColor(long elapsedNanos, long targetTimeSeconds) {
        return getTimerColor(elapsedNanos, targetTimeSeconds, 3);
    }

    /**
     * Gets the appropriate color for the timer based on elapsed time, target time, and warning buffer.
     *
     * @param elapsedNanos the elapsed time in nanoseconds
     * @param targetTimeSeconds the target time in seconds
     * @param warningBufferSeconds seconds before target to start showing warning color
     * @return the appropriate Color for the timer display
     */
    public static Color getTimerColor(long elapsedNanos, long targetTimeSeconds, int warningBufferSeconds) {
        long elapsedSeconds = elapsedNanos / 1_000_000_000;

        if (elapsedSeconds >= targetTimeSeconds) {
            return COLOR_OVER_TARGET;
        } else if (elapsedSeconds >= targetTimeSeconds - warningBufferSeconds) {
            return COLOR_WARNING;
        } else {
            return COLOR_NORMAL;
        }
    }

    /**
     * Determines if the elapsed time is under the target time.
     *
     * @param elapsedNanos the elapsed time in nanoseconds
     * @param targetTimeSeconds the target time in seconds
     * @return true if under target, false otherwise
     */
    public static boolean isUnderTarget(long elapsedNanos, long targetTimeSeconds) {
        return getElapsedSeconds(elapsedNanos) <= targetTimeSeconds;
    }
}
