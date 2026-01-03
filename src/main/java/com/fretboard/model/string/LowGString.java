package com.fretboard.model.string;

import com.fretboard.model.Frequency;
import com.fretboard.model.Note;

/* For 8-string guitars */
public final class LowGString extends GuitarString {

    public LowGString(byte numFrets) {
        Note note = Note.G;
        super((byte) 8, numFrets, new Frequency((byte) 1, note), note.getOrderedNotes());
    }

}
