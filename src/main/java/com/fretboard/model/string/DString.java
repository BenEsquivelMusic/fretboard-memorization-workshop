package com.fretboard.model.string;

import com.fretboard.model.Frequency;
import com.fretboard.model.Note;

public final class DString extends GuitarString {

    public DString(byte numFrets) {
        Note note = Note.D;
        super((byte) 4, numFrets, new Frequency((byte) 3, note), note.getOrderedNotes());
    }

    @Override
    public String getStringName() {
        return Note.D.name();
    }
}
