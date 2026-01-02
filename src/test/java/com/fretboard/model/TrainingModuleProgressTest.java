package com.fretboard.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrainingModuleProgressTest {

    @Test
    void classLoads() throws ClassNotFoundException {
        Class<?> cls = Class.forName("com.fretboard.model.TrainingModuleProgress");
        assertNotNull(cls, "TrainingModuleProgress class should be present on the classpath");
    }
}