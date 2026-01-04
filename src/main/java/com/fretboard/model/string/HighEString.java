package com.fretboard.model.string;

import com.fretboard.model.Frequency;
import com.fretboard.model.Note;

public final class HighEString extends GuitarString {

    public HighEString(byte numFrets) {
        Note note = Note.E;
        super((byte) 1, numFrets, new Frequency((byte) 4, note), note.getOrderedNotes());
    }

    @Override
    public String getStringName() {
        return "High E";
    }

}
