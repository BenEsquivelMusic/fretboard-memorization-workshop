package com.fretboard.service;

import com.fretboard.model.Frequency;
import com.fretboard.model.Note;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FrequencyMapServiceTest {

    @Test
    public void checkA4() {
        Note aNote = Note.A;
        byte octave = (byte) 4;
        assertEquals("A", aNote.getDisplayNote());
        Frequency frequency = new Frequency(octave, aNote);
        assertEquals(aNote, frequency.note());
        assertEquals(octave, frequency.octaveNumber());
        FrequencyMapService service = FrequencyMapService.getService();
        assertEquals(440.0, service.getMappedFrequencyNumericValue(frequency));
    }

    @Test
    public void checkG0() {
        Note gNote = Note.G;
        byte octave = (byte) 0;
        assertEquals("G", gNote.getDisplayNote());
        Frequency frequency = new Frequency(octave, gNote);
        assertEquals(gNote, frequency.note());
        assertEquals(octave, frequency.octaveNumber());
        FrequencyMapService service = FrequencyMapService.getService();
        assertEquals(24.499, service.getMappedFrequencyNumericValue(frequency));
    }

    @Test
    public void checkInvalidFrequencyWithTooLowOctave() {
        Frequency invalid = new Frequency((byte) -1, Note.E);
        assertThrows(IllegalArgumentException.class, () -> FrequencyMapService.getService().getMappedFrequencyNumericValue(invalid));
    }

    @Test
    public void checkInvalidFrequencyWithTooHighOctave() {
        Frequency invalid = new Frequency((byte) 25, Note.A_);
        assertThrows(IllegalArgumentException.class, () -> FrequencyMapService.getService().getMappedFrequencyNumericValue(invalid));
    }

}
