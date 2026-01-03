package com.fretboard.model.string;

import com.fretboard.model.Frequency;
import com.fretboard.model.Note;

/* For 7-string guitars */
public final class LowBString extends GuitarString {

    public LowBString(byte numFrets) {
        Note note = Note.B;
        super((byte) 7, numFrets, new Frequency((byte) 1, note), note.getOrderedNotes());
    }

}
