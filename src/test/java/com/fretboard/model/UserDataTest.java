package com.fretboard.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserDataTest {

    @Test
    void classLoads() throws ClassNotFoundException {
        Class<?> cls = Class.forName("com.fretboard.model.UserData");
        assertNotNull(cls, "UserData class should be present on the classpath");
    }

    @Test
    void checkUserData() {
        UserData userData = new UserData();
        assertFalse(userData.hasTrainingData());
        assertEquals(0, userData.getTotalSessions());
    }

    @Test
    void checkDefaultUserSettingsFromUserData() {
        UserData userData = new UserData();
        UserSettings settings = userData.getSettings();
        assertEquals(12, settings.getNumberOfFrets());
        assertEquals(44100.0f, settings.getInputSampleRate());
        assertEquals(4096, settings.getInputBufferSize());
    }
}