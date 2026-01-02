package com.fretboard.model;

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

}
