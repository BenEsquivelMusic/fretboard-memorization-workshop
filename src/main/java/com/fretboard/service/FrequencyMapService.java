package com.fretboard.service;

import com.fretboard.constants.FretBoardConstants;
import com.fretboard.model.Frequency;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Objects;

public final class FrequencyMapService {

    private static final ValueLayout.OfDouble DOUBLE_VALUE_LAYOUT = ValueLayout.JAVA_DOUBLE;
    private static final FrequencyMapService INSTANCE = new FrequencyMapService();
    private final MemorySegment[] noteFrequenciesByOctave;

    private FrequencyMapService() {
        this.noteFrequenciesByOctave = new MemorySegment[FretBoardConstants.MAX_AUDIBLE_OCTAVES];
        final int totalSize = FretBoardConstants.MAX_AUDIBLE_OCTAVES * FretBoardConstants.MAX_NOTES_IN_OCTAVE; //120
        Arena globalArena = Arena.global();
        MemorySegment parentSegment = globalArena.allocate(DOUBLE_VALUE_LAYOUT, totalSize);

        int currentOctave = 0;
        long offset = 0;
        while (currentOctave < FretBoardConstants.MAX_AUDIBLE_OCTAVES) {
            noteFrequenciesByOctave[currentOctave] = parentSegment.asSlice(
                    offset * DOUBLE_VALUE_LAYOUT.byteSize(), FretBoardConstants.MAX_NOTES_IN_OCTAVE * DOUBLE_VALUE_LAYOUT.byteSize());
            ++currentOctave;
            offset += FretBoardConstants.MAX_AUDIBLE_OCTAVES;
        }
        mapFrequencies();
    }

    public static FrequencyMapService getService() {
        return INSTANCE;
    }

    public double getMappedFrequencyNumericValue(Frequency frequency) {
        Objects.requireNonNull(frequency);
        Objects.requireNonNull(frequency.note());
        validateOctaveNumber(frequency.octaveNumber());
        MemorySegment noteFrequencyArray = noteFrequenciesByOctave[frequency.octaveNumber()];
        return noteFrequencyArray.getAtIndex(DOUBLE_VALUE_LAYOUT, frequency.note().ordinal());
    }

    private void validateOctaveNumber(int octaveNumber) {
        if (octaveNumber < 0 || octaveNumber > FretBoardConstants.MAX_AUDIBLE_OCTAVES) {
            throw new IllegalArgumentException("Invalid octave number provided: " + octaveNumber);
        }
    }

    private void mapFrequencies() {
        noteFrequenciesByOctave[0].setAtIndex(DOUBLE_VALUE_LAYOUT, 0, 16.351);
        noteFrequenciesByOctave[0].setAtIndex(DOUBLE_VALUE_LAYOUT, 1, 17.324);
        noteFrequenciesByOctave[0].setAtIndex(DOUBLE_VALUE_LAYOUT, 2, 18.354);
        noteFrequenciesByOctave[0].setAtIndex(DOUBLE_VALUE_LAYOUT, 3, 19.445);
        noteFrequenciesByOctave[0].setAtIndex(DOUBLE_VALUE_LAYOUT, 4, 20.601);
        noteFrequenciesByOctave[0].setAtIndex(DOUBLE_VALUE_LAYOUT, 5, 21.827);
        noteFrequenciesByOctave[0].setAtIndex(DOUBLE_VALUE_LAYOUT, 6, 23.124);
        noteFrequenciesByOctave[0].setAtIndex(DOUBLE_VALUE_LAYOUT, 7, 24.499);
        noteFrequenciesByOctave[0].setAtIndex(DOUBLE_VALUE_LAYOUT, 8, 25.956);
        noteFrequenciesByOctave[0].setAtIndex(DOUBLE_VALUE_LAYOUT, 9, 27.5);
        noteFrequenciesByOctave[0].setAtIndex(DOUBLE_VALUE_LAYOUT, 10, 29.135);
        noteFrequenciesByOctave[0].setAtIndex(DOUBLE_VALUE_LAYOUT, 11, 30.868);
        noteFrequenciesByOctave[1].setAtIndex(DOUBLE_VALUE_LAYOUT, 0, 32.703);
        noteFrequenciesByOctave[1].setAtIndex(DOUBLE_VALUE_LAYOUT, 1, 34.648);
        noteFrequenciesByOctave[1].setAtIndex(DOUBLE_VALUE_LAYOUT, 2, 36.708);
        noteFrequenciesByOctave[1].setAtIndex(DOUBLE_VALUE_LAYOUT, 3, 38.891);
        noteFrequenciesByOctave[1].setAtIndex(DOUBLE_VALUE_LAYOUT, 4, 41.203);
        noteFrequenciesByOctave[1].setAtIndex(DOUBLE_VALUE_LAYOUT, 5, 43.654);
        noteFrequenciesByOctave[1].setAtIndex(DOUBLE_VALUE_LAYOUT, 6, 46.249);
        noteFrequenciesByOctave[1].setAtIndex(DOUBLE_VALUE_LAYOUT, 7, 48.999);
        noteFrequenciesByOctave[1].setAtIndex(DOUBLE_VALUE_LAYOUT, 8, 51.913);
        noteFrequenciesByOctave[1].setAtIndex(DOUBLE_VALUE_LAYOUT, 9, 55);
        noteFrequenciesByOctave[1].setAtIndex(DOUBLE_VALUE_LAYOUT, 10, 58.27);
        noteFrequenciesByOctave[1].setAtIndex(DOUBLE_VALUE_LAYOUT, 11, 61.735);
        noteFrequenciesByOctave[2].setAtIndex(DOUBLE_VALUE_LAYOUT, 0, 65.406);
        noteFrequenciesByOctave[2].setAtIndex(DOUBLE_VALUE_LAYOUT, 1, 69.296);
        noteFrequenciesByOctave[2].setAtIndex(DOUBLE_VALUE_LAYOUT, 2, 73.416);
        noteFrequenciesByOctave[2].setAtIndex(DOUBLE_VALUE_LAYOUT, 3, 77.782);
        noteFrequenciesByOctave[2].setAtIndex(DOUBLE_VALUE_LAYOUT, 4, 82.407);
        noteFrequenciesByOctave[2].setAtIndex(DOUBLE_VALUE_LAYOUT, 5, 87.307);
        noteFrequenciesByOctave[2].setAtIndex(DOUBLE_VALUE_LAYOUT, 6, 92.499);
        noteFrequenciesByOctave[2].setAtIndex(DOUBLE_VALUE_LAYOUT, 7, 97.999);
        noteFrequenciesByOctave[2].setAtIndex(DOUBLE_VALUE_LAYOUT, 8, 103.826);
        noteFrequenciesByOctave[2].setAtIndex(DOUBLE_VALUE_LAYOUT, 9, 110);
        noteFrequenciesByOctave[2].setAtIndex(DOUBLE_VALUE_LAYOUT, 10, 116.541);
        noteFrequenciesByOctave[2].setAtIndex(DOUBLE_VALUE_LAYOUT, 11, 123.471);
        noteFrequenciesByOctave[3].setAtIndex(DOUBLE_VALUE_LAYOUT, 0, 130.813);
        noteFrequenciesByOctave[3].setAtIndex(DOUBLE_VALUE_LAYOUT, 1, 138.591);
        noteFrequenciesByOctave[3].setAtIndex(DOUBLE_VALUE_LAYOUT, 2, 146.832);
        noteFrequenciesByOctave[3].setAtIndex(DOUBLE_VALUE_LAYOUT, 3, 155.563);
        noteFrequenciesByOctave[3].setAtIndex(DOUBLE_VALUE_LAYOUT, 4, 164.814);
        noteFrequenciesByOctave[3].setAtIndex(DOUBLE_VALUE_LAYOUT, 5, 174.614);
        noteFrequenciesByOctave[3].setAtIndex(DOUBLE_VALUE_LAYOUT, 6, 184.997);
        noteFrequenciesByOctave[3].setAtIndex(DOUBLE_VALUE_LAYOUT, 7, 195.998);
        noteFrequenciesByOctave[3].setAtIndex(DOUBLE_VALUE_LAYOUT, 8, 207.652);
        noteFrequenciesByOctave[3].setAtIndex(DOUBLE_VALUE_LAYOUT, 9, 220);
        noteFrequenciesByOctave[3].setAtIndex(DOUBLE_VALUE_LAYOUT, 10, 233.082);
        noteFrequenciesByOctave[3].setAtIndex(DOUBLE_VALUE_LAYOUT, 11, 246.942);
        noteFrequenciesByOctave[4].setAtIndex(DOUBLE_VALUE_LAYOUT, 0, 261.626);
        noteFrequenciesByOctave[4].setAtIndex(DOUBLE_VALUE_LAYOUT, 1, 277.183);
        noteFrequenciesByOctave[4].setAtIndex(DOUBLE_VALUE_LAYOUT, 2, 293.665);
        noteFrequenciesByOctave[4].setAtIndex(DOUBLE_VALUE_LAYOUT, 3, 311.127);
        noteFrequenciesByOctave[4].setAtIndex(DOUBLE_VALUE_LAYOUT, 4, 329.628);
        noteFrequenciesByOctave[4].setAtIndex(DOUBLE_VALUE_LAYOUT, 5, 349.228);
        noteFrequenciesByOctave[4].setAtIndex(DOUBLE_VALUE_LAYOUT, 6, 369.994);
        noteFrequenciesByOctave[4].setAtIndex(DOUBLE_VALUE_LAYOUT, 7, 391.995);
        noteFrequenciesByOctave[4].setAtIndex(DOUBLE_VALUE_LAYOUT, 8, 415.305);
        noteFrequenciesByOctave[4].setAtIndex(DOUBLE_VALUE_LAYOUT, 9, 440);
        noteFrequenciesByOctave[4].setAtIndex(DOUBLE_VALUE_LAYOUT, 10, 466.164);
        noteFrequenciesByOctave[4].setAtIndex(DOUBLE_VALUE_LAYOUT, 11, 493.883);
        noteFrequenciesByOctave[5].setAtIndex(DOUBLE_VALUE_LAYOUT, 0, 523.251);
        noteFrequenciesByOctave[5].setAtIndex(DOUBLE_VALUE_LAYOUT, 1, 554.365);
        noteFrequenciesByOctave[5].setAtIndex(DOUBLE_VALUE_LAYOUT, 2, 587.33);
        noteFrequenciesByOctave[5].setAtIndex(DOUBLE_VALUE_LAYOUT, 3, 622.254);
        noteFrequenciesByOctave[5].setAtIndex(DOUBLE_VALUE_LAYOUT, 4, 659.255);
        noteFrequenciesByOctave[5].setAtIndex(DOUBLE_VALUE_LAYOUT, 5, 698.456);
        noteFrequenciesByOctave[5].setAtIndex(DOUBLE_VALUE_LAYOUT, 6, 739.989);
        noteFrequenciesByOctave[5].setAtIndex(DOUBLE_VALUE_LAYOUT, 7, 783.991);
        noteFrequenciesByOctave[5].setAtIndex(DOUBLE_VALUE_LAYOUT, 8, 830.609);
        noteFrequenciesByOctave[5].setAtIndex(DOUBLE_VALUE_LAYOUT, 9, 880);
        noteFrequenciesByOctave[5].setAtIndex(DOUBLE_VALUE_LAYOUT, 10, 932.328);
        noteFrequenciesByOctave[5].setAtIndex(DOUBLE_VALUE_LAYOUT, 11, 987.767);
        noteFrequenciesByOctave[6].setAtIndex(DOUBLE_VALUE_LAYOUT, 0, 1046.502);
        noteFrequenciesByOctave[6].setAtIndex(DOUBLE_VALUE_LAYOUT, 1, 1108.731);
        noteFrequenciesByOctave[6].setAtIndex(DOUBLE_VALUE_LAYOUT, 2, 1174.659);
        noteFrequenciesByOctave[6].setAtIndex(DOUBLE_VALUE_LAYOUT, 3, 1244.508);
        noteFrequenciesByOctave[6].setAtIndex(DOUBLE_VALUE_LAYOUT, 4, 1318.51);
        noteFrequenciesByOctave[6].setAtIndex(DOUBLE_VALUE_LAYOUT, 5, 1396.913);
        noteFrequenciesByOctave[6].setAtIndex(DOUBLE_VALUE_LAYOUT, 6, 1479.978);
        noteFrequenciesByOctave[6].setAtIndex(DOUBLE_VALUE_LAYOUT, 7, 1567.982);
        noteFrequenciesByOctave[6].setAtIndex(DOUBLE_VALUE_LAYOUT, 8, 1661.219);
        noteFrequenciesByOctave[6].setAtIndex(DOUBLE_VALUE_LAYOUT, 9, 1760);
        noteFrequenciesByOctave[6].setAtIndex(DOUBLE_VALUE_LAYOUT, 10, 1864.655);
        noteFrequenciesByOctave[6].setAtIndex(DOUBLE_VALUE_LAYOUT, 11, 1975.533);
        noteFrequenciesByOctave[7].setAtIndex(DOUBLE_VALUE_LAYOUT, 0, 2093.005);
        noteFrequenciesByOctave[7].setAtIndex(DOUBLE_VALUE_LAYOUT, 1, 2217.461);
        noteFrequenciesByOctave[7].setAtIndex(DOUBLE_VALUE_LAYOUT, 2, 2349.318);
        noteFrequenciesByOctave[7].setAtIndex(DOUBLE_VALUE_LAYOUT, 3, 2489.016);
        noteFrequenciesByOctave[7].setAtIndex(DOUBLE_VALUE_LAYOUT, 4, 2637.021);
        noteFrequenciesByOctave[7].setAtIndex(DOUBLE_VALUE_LAYOUT, 5, 2793.826);
        noteFrequenciesByOctave[7].setAtIndex(DOUBLE_VALUE_LAYOUT, 6, 2959.955);
        noteFrequenciesByOctave[7].setAtIndex(DOUBLE_VALUE_LAYOUT, 7, 3135.964);
        noteFrequenciesByOctave[7].setAtIndex(DOUBLE_VALUE_LAYOUT, 8, 3322.438);
        noteFrequenciesByOctave[7].setAtIndex(DOUBLE_VALUE_LAYOUT, 9, 3520);
        noteFrequenciesByOctave[7].setAtIndex(DOUBLE_VALUE_LAYOUT, 10, 3729.31);
        noteFrequenciesByOctave[7].setAtIndex(DOUBLE_VALUE_LAYOUT, 11, 3951.066);
        noteFrequenciesByOctave[8].setAtIndex(DOUBLE_VALUE_LAYOUT, 0, 4186.009);
        noteFrequenciesByOctave[8].setAtIndex(DOUBLE_VALUE_LAYOUT, 1, 4434.922);
        noteFrequenciesByOctave[8].setAtIndex(DOUBLE_VALUE_LAYOUT, 2, 4698.636);
        noteFrequenciesByOctave[8].setAtIndex(DOUBLE_VALUE_LAYOUT, 3, 4978.032);
        noteFrequenciesByOctave[8].setAtIndex(DOUBLE_VALUE_LAYOUT, 4, 5274.042);
        noteFrequenciesByOctave[8].setAtIndex(DOUBLE_VALUE_LAYOUT, 5, 5587.652);
        noteFrequenciesByOctave[8].setAtIndex(DOUBLE_VALUE_LAYOUT, 6, 5919.91);
        noteFrequenciesByOctave[8].setAtIndex(DOUBLE_VALUE_LAYOUT, 7, 6271.928);
        noteFrequenciesByOctave[8].setAtIndex(DOUBLE_VALUE_LAYOUT, 8, 6644.876);
        noteFrequenciesByOctave[8].setAtIndex(DOUBLE_VALUE_LAYOUT, 9, 7040);
        noteFrequenciesByOctave[8].setAtIndex(DOUBLE_VALUE_LAYOUT, 10, 7458.62);
        noteFrequenciesByOctave[8].setAtIndex(DOUBLE_VALUE_LAYOUT, 11, 7902.132);
        noteFrequenciesByOctave[9].setAtIndex(DOUBLE_VALUE_LAYOUT, 0, 8372.018);
        noteFrequenciesByOctave[9].setAtIndex(DOUBLE_VALUE_LAYOUT, 1, 8869.844);
        noteFrequenciesByOctave[9].setAtIndex(DOUBLE_VALUE_LAYOUT, 2, 9397.272);
        noteFrequenciesByOctave[9].setAtIndex(DOUBLE_VALUE_LAYOUT, 3, 9956.064);
        noteFrequenciesByOctave[9].setAtIndex(DOUBLE_VALUE_LAYOUT, 4, 10548.084);
        noteFrequenciesByOctave[9].setAtIndex(DOUBLE_VALUE_LAYOUT, 5, 11175.304);
        noteFrequenciesByOctave[9].setAtIndex(DOUBLE_VALUE_LAYOUT, 6, 11839.82);
        noteFrequenciesByOctave[9].setAtIndex(DOUBLE_VALUE_LAYOUT, 7, 12543.856);
        noteFrequenciesByOctave[9].setAtIndex(DOUBLE_VALUE_LAYOUT, 8, 13289.752);
        noteFrequenciesByOctave[9].setAtIndex(DOUBLE_VALUE_LAYOUT, 9, 14080);
        noteFrequenciesByOctave[9].setAtIndex(DOUBLE_VALUE_LAYOUT, 10, 14917.24);
        noteFrequenciesByOctave[9].setAtIndex(DOUBLE_VALUE_LAYOUT, 11, 15804.264);
    }

}
