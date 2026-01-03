package com.fretboard.model;

import com.fretboard.constants.FretBoardConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

public enum Note {

    C("C"),
    C_("C♯/D♭"),
    D("D"),
    D_("D♯/E♭"),
    E("E"),
    F("F"),
    F_("F♯/G♭"),
    G("G"),
    G_("G♯/A♭"),
    A("A"),
    A_("A♯/B♭"),
    B("B");

    private final String displayNote;

    Note(String displayNote) {
        this.displayNote = displayNote;
    }

    public String getDisplayNote() {
        return displayNote;
    }

    public LinkedHashSet<Note> getOrderedNotes() {
        Note[] naturalOrder = values();
        if (C.equals(this)) return new LinkedHashSet<>(Arrays.asList(naturalOrder));
        Note[] firstNoteGroup = Arrays.copyOfRange(naturalOrder, ordinal(), naturalOrder.length);
        Note[] lastNoteGroup = Arrays.copyOfRange(naturalOrder, 0, ordinal());
        LinkedHashSet<Note> orderedNotes = new LinkedHashSet<>(FretBoardConstants.MAX_NOTES_IN_OCTAVE);
        Collections.addAll(orderedNotes, firstNoteGroup);
        Collections.addAll(orderedNotes, lastNoteGroup);
        return orderedNotes;
    }

}
