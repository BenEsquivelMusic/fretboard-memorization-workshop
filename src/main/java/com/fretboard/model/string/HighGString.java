package com.fretboard.model.string;

import com.fretboard.model.Frequency;
import com.fretboard.model.Note;

public final class HighGString extends GuitarString {

    public HighGString(byte numFrets) {
        Note note = Note.G;
        super((byte) 3, numFrets, new Frequency((byte) 3, note), note.getOrderedNotes());
    }

    @Override
    public String getStringName() {
        return Note.G.name();
    }
}
