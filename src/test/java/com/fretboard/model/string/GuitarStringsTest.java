package com.fretboard.model.string;

import com.fretboard.model.UserSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class GuitarStringsTest {

    @Test
    public void checkStandard6StringGuitar() {
        GuitarStrings guitarStrings = new GuitarStrings(new UserSettings());
        GuitarString[] stringArray = guitarStrings.getAll();
        assertEquals(6, stringArray.length);
        assertInstanceOf(HighEString.class, stringArray[0]);
        assertInstanceOf(HighBString.class, stringArray[1]);
        assertInstanceOf(HighGString.class, stringArray[2]);
        assertInstanceOf(DString.class, stringArray[3]);
        assertInstanceOf(AString.class, stringArray[4]);
        assertInstanceOf(LowEString.class, stringArray[5]);

        for (GuitarString guitarString : stringArray) {
            assertEquals(UserSettings.DEFAULT_FRET_COUNT, guitarString.getFretBoardFrequencies().size());
        }
    }

    @Test
    public void check7StringGuitar() {
        UserSettings userSettings = new UserSettings();
        userSettings.setNumberOfStrings(7);
        GuitarStrings guitarStrings = new GuitarStrings(userSettings);
        GuitarString[] stringArray = guitarStrings.getAll();
        assertEquals(7, stringArray.length);
        assertInstanceOf(HighEString.class, stringArray[0]);
        assertInstanceOf(HighBString.class, stringArray[1]);
        assertInstanceOf(HighGString.class, stringArray[2]);
        assertInstanceOf(DString.class, stringArray[3]);
        assertInstanceOf(AString.class, stringArray[4]);
        assertInstanceOf(LowEString.class, stringArray[5]);
        assertInstanceOf(LowBString.class, stringArray[6]);

        for (GuitarString guitarString : stringArray) {
            assertEquals(UserSettings.DEFAULT_FRET_COUNT, guitarString.getFretBoardFrequencies().size());
        }
    }

    @Test
    public void check8StringGuitar() {
        UserSettings userSettings = new UserSettings();
        userSettings.setNumberOfStrings(8);
        userSettings.setNumberOfFrets(28);
        GuitarStrings guitarStrings = new GuitarStrings(userSettings);
        GuitarString[] stringArray = guitarStrings.getAll();
        assertEquals(8, stringArray.length);
        assertInstanceOf(HighEString.class, stringArray[0]);
        assertInstanceOf(HighBString.class, stringArray[1]);
        assertInstanceOf(HighGString.class, stringArray[2]);
        assertInstanceOf(DString.class, stringArray[3]);
        assertInstanceOf(AString.class, stringArray[4]);
        assertInstanceOf(LowEString.class, stringArray[5]);
        assertInstanceOf(LowBString.class, stringArray[6]);
        assertInstanceOf(LowGString.class, stringArray[7]);

        for (GuitarString guitarString : stringArray) {
            assertEquals(28, guitarString.getFretBoardFrequencies().size());
        }
    }
}
