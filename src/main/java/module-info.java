module com.fretboard {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires java.logging;
    requires jdk.compiler;

    opens com.fretboard to javafx.fxml;
    opens com.fretboard.controller to javafx.fxml;
    opens com.fretboard.model to javafx.fxml;

    exports com.fretboard;
    exports com.fretboard.controller;
    exports com.fretboard.model;
    exports com.fretboard.module;
    exports com.fretboard.service;
    exports com.fretboard.model.string;
    opens com.fretboard.model.string to javafx.fxml;
    exports com.fretboard.constants;
    opens com.fretboard.constants to javafx.fxml;
}