module com.fretboard {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires java.logging;

    opens com.fretboard to javafx.fxml;
    opens com.fretboard.controller to javafx.fxml;
    opens com.fretboard.model to javafx.fxml;

    exports com.fretboard;
    exports com.fretboard.controller;
    exports com.fretboard.model;
    exports com.fretboard.module;
    exports com.fretboard.service;
}