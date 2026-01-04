package com.fretboard.model.string;

import com.fretboard.model.Frequency;
import com.fretboard.model.Note;

public final class HighBString extends GuitarString {

    public HighBString(byte numFrets) {
        Note note = Note.B;
        super((byte) 2, numFrets, new Frequency((byte) 3, note), note.getOrderedNotes());
    }

    @Override
    public String getStringName() {
        return Note.B.name();
    }
}
