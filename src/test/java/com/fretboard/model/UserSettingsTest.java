package com.fretboard.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserSettingsTest {

    @Test
    void classLoads() throws ClassNotFoundException {
        Class<?> cls = Class.forName("com.fretboard.model.UserSettings");
        assertNotNull(cls, "UserSettings class should be present on the classpath");
    }
}