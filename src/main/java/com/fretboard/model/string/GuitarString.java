package com.fretboard.model.string;

import com.fretboard.constants.FretBoardConstants;
import com.fretboard.model.Frequency;
import com.fretboard.model.FrequencyRange;
import com.fretboard.model.Note;
import com.fretboard.model.UserSettings;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public sealed abstract class GuitarString permits
        AString,
        DString,
        HighBString,
        HighEString,
        HighGString,
        LowBString,
        LowEString,
        LowGString {

    private final byte stringNumber;
    private final Frequency openString;
    private final Frequency[] frets;

    public GuitarString(byte stringNum,
                        byte numFrets,
                        Frequency openFrequency,
                        LinkedHashSet<Note> orderedNotes) {
        if (stringNum <= 0 || stringNum > UserSettings.MAX_STRING_COUNT) {
            throw new IllegalArgumentException("Invalid string number provided: " + stringNum);
        }
        if (numFrets < UserSettings.MIN_FRET_COUNT || numFrets > UserSettings.MAX_FRET_COUNT) {
            throw new IllegalArgumentException("Invalid fret count provided: " + numFrets);
        }
        Objects.requireNonNull(openFrequency);
        Objects.requireNonNull(orderedNotes);
        if (orderedNotes.size() != FretBoardConstants.MAX_NOTES_IN_OCTAVE) {
            throw new IllegalArgumentException("Expected 12 notes. Found: " + orderedNotes.size());
        }
        if (!orderedNotes.getFirst().equals(openFrequency.note())) {
            throw new IllegalArgumentException("Open string note does not equal the first note in fretboard object: " + openFrequency);
        }
        this.openString = openFrequency;
        this.stringNumber = stringNum;
        this.frets = new Frequency[numFrets];
        buildFretBoard(orderedNotes.toArray(new Note[orderedNotes.size()]));
    }

    public Frequency getOpenString() {
        return openString;
    }

    /* This excludes the open string */
    public List<Frequency> getFretBoardFrequencies() {
        return List.of(frets);
    }

    public FrequencyRange getFrequencyRange() {
        int numFrets = frets.length;
        return new FrequencyRange(openString, frets[numFrets - 1]);
    }

    public byte getStringNumber() {
        return stringNumber;
    }

    private void buildFretBoard(Note[] orderedNotes) {
        int noteOrdinal = openString.note().ordinal();
        byte octaveNumber = openString.octaveNumber();
        int fretCounter = 0;
        int numFrets = frets.length;
        Frequency[] tempFrets = new Frequency[numFrets + 1];
        while (fretCounter < numFrets) {
            int fretIndex = fretCounter + 1;
            Note note = orderedNotes[getNoteIndex(fretIndex)];
            if (fretIndex == 1 && Note.C.equals(note)) {
                tempFrets[fretIndex] = new Frequency((byte) (octaveNumber + 1), note);
            } else {
                tempFrets[fretIndex] = new Frequency(octaveNumber, note);
            }
            ++noteOrdinal;
            if (noteOrdinal >= FretBoardConstants.MAX_NOTES_IN_OCTAVE - 1) {
                noteOrdinal = 0;
                ++octaveNumber;
            }
            ++fretCounter;
        }
        /* Kick out the very first note in tempFrets because that's the open string */
        IntStream.range(1, tempFrets.length).forEach(fretIndex -> frets[fretIndex - 1] = tempFrets[fretIndex]);
    }

    private int getNoteIndex(int fretCounter) {
        if (fretCounter < FretBoardConstants.MAX_NOTES_IN_OCTAVE) {
            return fretCounter;
        }
        return fretCounter % FretBoardConstants.MAX_NOTES_IN_OCTAVE;
    }

}
