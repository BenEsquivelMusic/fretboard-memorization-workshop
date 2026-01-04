package com.fretboard.util;

import com.fretboard.model.Frequency;
import com.fretboard.service.FrequencyMapService;

import java.util.Optional;
import java.util.Set;

/**
 * Utility class for matching detected frequencies to target frequencies.
 * Consolidates duplicate frequency matching logic from multiple training modules.
 */
public final class FrequencyMatcherUtil {

    /**
     * Default tolerance in cents for frequency matching.
     * 50 cents = half a semitone, which is a reasonable tolerance for pitch detection.
     */
    public static final double DEFAULT_TOLERANCE_CENTS = 50.0;

    private FrequencyMatcherUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Calculates the interval difference in cents between two frequencies.
     * Cents are a logarithmic unit of measure for musical intervals.
     * 100 cents = 1 semitone, 1200 cents = 1 octave.
     *
     * @param detectedHz the detected frequency in Hz
     * @param targetHz the target frequency in Hz
     * @return the difference in cents (positive if detected is higher, negative if lower)
     */
    public static double calculateCents(double detectedHz, double targetHz) {
        return 1200 * Math.log(detectedHz / targetHz) / Math.log(2);
    }

    /**
     * Finds a matching frequency from a set of target frequencies.
     * Uses the default tolerance of 50 cents.
     *
     * @param detectedHz the detected frequency in Hz
     * @param targets the set of target Frequency objects to match against
     * @param frequencyMapService the service to get frequency values in Hz
     * @return an Optional containing the matched Frequency, or empty if no match found
     */
    public static Optional<Frequency> findMatch(
            double detectedHz,
            Set<Frequency> targets,
            FrequencyMapService frequencyMapService) {
        return findMatch(detectedHz, targets, frequencyMapService, DEFAULT_TOLERANCE_CENTS);
    }

    /**
     * Finds a matching frequency from a set of target frequencies with a custom tolerance.
     *
     * @param detectedHz the detected frequency in Hz
     * @param targets the set of target Frequency objects to match against
     * @param frequencyMapService the service to get frequency values in Hz
     * @param toleranceCents the tolerance in cents for matching
     * @return an Optional containing the matched Frequency, or empty if no match found
     */
    public static Optional<Frequency> findMatch(
            double detectedHz,
            Set<Frequency> targets,
            FrequencyMapService frequencyMapService,
            double toleranceCents) {
        
        if (detectedHz <= 0 || targets == null || targets.isEmpty()) {
            return Optional.empty();
        }

        for (Frequency target : targets) {
            double targetHz = frequencyMapService.getMappedFrequencyNumericValue(target);
            double cents = calculateCents(detectedHz, targetHz);

            if (Math.abs(cents) <= toleranceCents) {
                return Optional.of(target);
            }
        }

        return Optional.empty();
    }

    /**
     * Checks if a detected frequency matches a specific target frequency.
     * Uses the default tolerance of 50 cents.
     *
     * @param detectedHz the detected frequency in Hz
     * @param target the target Frequency object
     * @param frequencyMapService the service to get frequency values in Hz
     * @return true if the frequencies match within tolerance, false otherwise
     */
    public static boolean isMatch(
            double detectedHz,
            Frequency target,
            FrequencyMapService frequencyMapService) {
        return isMatch(detectedHz, target, frequencyMapService, DEFAULT_TOLERANCE_CENTS);
    }

    /**
     * Checks if a detected frequency matches a specific target frequency with a custom tolerance.
     *
     * @param detectedHz the detected frequency in Hz
     * @param target the target Frequency object
     * @param frequencyMapService the service to get frequency values in Hz
     * @param toleranceCents the tolerance in cents for matching
     * @return true if the frequencies match within tolerance, false otherwise
     */
    public static boolean isMatch(
            double detectedHz,
            Frequency target,
            FrequencyMapService frequencyMapService,
            double toleranceCents) {
        
        if (detectedHz <= 0 || target == null) {
            return false;
        }

        double targetHz = frequencyMapService.getMappedFrequencyNumericValue(target);
        double cents = calculateCents(detectedHz, targetHz);

        return Math.abs(cents) <= toleranceCents;
    }
}
