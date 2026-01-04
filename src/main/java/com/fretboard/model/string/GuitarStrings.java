package com.fretboard.model.string;

import com.fretboard.model.UserSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;

public final class GuitarStrings {

    private final GuitarStringList guitarStrings;

    public GuitarStrings(UserSettings userSettings) {
        this.guitarStrings = new GuitarStringList((byte) userSettings.getNumberOfFrets(), (byte) userSettings.getNumberOfStrings());
    }

    public GuitarString[] getAll() {
        return guitarStrings.toArray(new GuitarString[guitarStrings.size()]);
    }

    private static final class GuitarStringList extends ArrayList<GuitarString> {

        private GuitarStringList(byte numFrets, byte numStrings) {
            super(numStrings);
            addGuitarStrings(numFrets, numStrings);
        }

        private void addGuitarStrings(byte numFrets, byte numStrings) {
            for (Class<?> guitarStringClass : GuitarString.class.getPermittedSubclasses()) {
                try {
                    Constructor<?> guitarClassConstructor = guitarStringClass.getConstructor(byte.class);
                    GuitarString guitarString = (GuitarString) guitarClassConstructor.newInstance(numFrets);
                    if (guitarString.getStringNumber() <= numStrings) {
                        add(guitarString);
                    }
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                         IllegalAccessException e) {
                    throw new IllegalArgumentException(e);
                }
            }
            sort(Comparator.comparingInt(GuitarString::getStringNumber));
        }
    }
}
