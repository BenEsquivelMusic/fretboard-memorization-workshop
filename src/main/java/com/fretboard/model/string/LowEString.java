package com.fretboard.model.string;

import com.fretboard.model.Frequency;
import com.fretboard.model.Note;

public final class LowEString extends GuitarString {

    public LowEString(byte numFrets) {
        Note note = Note.E;
        super((byte) 6, numFrets, new Frequency((byte) 2, note), note.getOrderedNotes());
    }

}
