package com.fretboard.model.string;

import com.fretboard.model.Frequency;
import com.fretboard.model.Note;

public final class AString extends GuitarString {

    public AString(byte numFrets) {
        Note note = Note.A;
        super((byte) 5, numFrets, new Frequency((byte) 2, note), note.getOrderedNotes());
    }

}
