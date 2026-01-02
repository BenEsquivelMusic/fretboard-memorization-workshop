# Fretboard Memorization Workshop

A JavaFX application for helping guitar players improve fretboard memorization.

Note: this is currently a work in progress.

TODO:

- Add modules for fretboard memorization
- Create a user guide
- Allow users to track and visualize their progress over time

This README explains how to compile and run the project using Maven.

---

## Prerequisites

- JDK 25 or newer
- Apache Maven 3.6.0 or newer.

Verify installations:

```bash
java -version
mvn -v
```

---

## Build (compile + package)

The application can be built with javafx-maven-plugin using the plugin's jlink goal from the project root:

```bash
mvn clean javafx:jlink
```

---

## Run

There are two common ways to run a JavaFX Maven-built project.

A) Run the compiled application

1. Build the project:

```bash
mvn clean javafx:jlink
```

2. Navigate to the target/FretboardApplication/bin folder
3. Run the application via FretboardApplication file (.bat for windows, .sh for linux)

B) Use the plugin's run goal from the project root:

```bash
mvn javafx:run
```

---

## IDE

You can also import the project into IntelliJ IDEA, Eclipse, or VS Code as a Maven project and run it from the IDE. Make sure the IDE is configured to use a compatible JDK and that JavaFX is available in the project classpath or configured via the plugin.


---

## Contributing

Contributions are welcome. Please open issues or pull requests on the repository for bug reports or enhancements.
