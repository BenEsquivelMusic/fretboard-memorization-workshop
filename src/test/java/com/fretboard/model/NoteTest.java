package com.fretboard.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NoteTest {

    @Test
    public void checkNaturalChromaticOrder() {
        Note c = Note.C;
        assertArrayEquals(c.getOrderedNotes().toArray(), Note.values());
    }

    @Test
    public void checkDisplayNameForASharp(){
        Note aSharp = Note.A_;
        assertEquals("A♯/B♭", aSharp.getDisplayNote());
    }

}
