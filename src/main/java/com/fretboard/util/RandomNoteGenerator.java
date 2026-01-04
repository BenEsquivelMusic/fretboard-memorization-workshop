package com.fretboard.util;

import com.fretboard.model.Note;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Utility class for generating random sequences of notes.
 * Consolidates duplicate random note generation logic from multiple training modules.
 */
public final class RandomNoteGenerator {

    private final Random random;

    /**
     * Creates a new RandomNoteGenerator with a default Random instance.
     */
    public RandomNoteGenerator() {
        this.random = new Random();
    }

    /**
     * Creates a new RandomNoteGenerator with the specified Random instance.
     * Useful for testing with a seeded random.
     *
     * @param random the Random instance to use
     */
    public RandomNoteGenerator(Random random) {
        this.random = random;
    }

    /**
     * Generates a list of unique random notes.
     * The notes are shuffled and the first 'count' notes are returned.
     * If count exceeds the total number of notes (12), all notes are returned shuffled.
     *
     * @param count the number of notes to generate
     * @return a list of randomly selected unique notes
     */
    public List<Note> generateUniqueNotes(int count) {
        Note[] allNotes = Note.values();
        List<Note> availableNotes = new ArrayList<>(List.of(allNotes));
        Collections.shuffle(availableNotes, random);

        int actualCount = Math.min(count, availableNotes.size());
        return new ArrayList<>(availableNotes.subList(0, actualCount));
    }

    /**
     * Generates a single random note.
     *
     * @return a randomly selected note
     */
    public Note generateSingleNote() {
        Note[] allNotes = Note.values();
        return allNotes[random.nextInt(allNotes.length)];
    }

    /**
     * Generates a list of random notes (may contain duplicates).
     *
     * @param count the number of notes to generate
     * @return a list of randomly selected notes
     */
    public List<Note> generateNotes(int count) {
        Note[] allNotes = Note.values();
        List<Note> notes = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            notes.add(allNotes[random.nextInt(allNotes.length)]);
        }
        
        return notes;
    }
}
