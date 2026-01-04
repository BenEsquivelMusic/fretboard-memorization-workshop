package com.fretboard.util;

import com.fretboard.model.Frequency;
import com.fretboard.model.Note;
import com.fretboard.model.string.GuitarString;
import com.fretboard.model.string.GuitarStrings;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for finding notes on guitar strings and fretboard.
 * Consolidates duplicate note-finding logic from multiple training modules.
 */
public final class NoteFinderUtil {

    private NoteFinderUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Finds all occurrences of a note on a single guitar string.
     * This includes both the open string and all fretted positions.
     *
     * @param note the note to find
     * @param string the guitar string to search
     * @return a set of Frequency objects representing all locations of the note on the string
     */
    public static Set<Frequency> findNoteOnString(Note note, GuitarString string) {
        Set<Frequency> locations = new HashSet<>();

        // Check open string
        if (string.getOpenString().note() == note) {
            locations.add(string.getOpenString());
        }

        // Check all frets
        for (Frequency freq : string.getFretBoardFrequencies()) {
            if (freq.note() == note) {
                locations.add(freq);
            }
        }

        return locations;
    }

    /**
     * Finds all occurrences of a note across the entire fretboard (all strings).
     * This includes both open strings and all fretted positions on every string.
     *
     * @param note the note to find
     * @param guitarStrings the guitar strings configuration
     * @return a set of Frequency objects representing all locations of the note on the fretboard
     */
    public static Set<Frequency> findNoteOnFretboard(Note note, GuitarStrings guitarStrings) {
        Set<Frequency> locations = new HashSet<>();

        for (GuitarString string : guitarStrings.getAll()) {
            locations.addAll(findNoteOnString(note, string));
        }

        return locations;
    }
}
