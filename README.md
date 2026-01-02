# Fretboard Memorization Workshop

A JavaFX application for helping guitar players improve fretboard memorization.

Note: this is currently a work in progress.

TODO:

- Add modules for fretboard memorization.
- Allow users to track and visualize their progress over time.

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

A) If the project is configured with the javafx-maven-plugin (recommended)

Use the plugin's run goal from the project root:

```bash
mvn javafx:run
```

B) Run the packaged JAR (manual module-path approach)

1. Build the project:

```bash
mvn clean package
```

2. Locate the produced JAR in `target/`. The filename usually looks like:
`target/<artifactId>-<version>.jar`.

3. If the JAR is a plain (non-modular) JAR and JavaFX is not on the classpath, run with the JavaFX SDK module path:

Linux / macOS example:

```bash
java --module-path /path/to/javafx-sdk/lib \
  --add-modules=javafx.controls,javafx.fxml \
  -jar target/fretboard-memorization-workshop-<version>.jar
```

Windows (PowerShell) example:

```powershell
java --module-path "C:\path\to\javafx-sdk\lib" `
  --add-modules=javafx.controls,javafx.fxml `
  -jar target\fretboard-memorization-workshop-<version>.jar
```

Notes:
- Replace `/path/to/javafx-sdk/lib` (or the Windows path) with the path to your JavaFX SDK `lib` directory.
- Replace `<version>` with the actual artifact version from `target/`.

If the project is packaged as a "fat" (uber) JAR that already contains JavaFX dependencies, you can run:

```bash
java -jar target/fretboard-memorization-workshop-<version>.jar
```

---

## IDE

You can also import the project into IntelliJ IDEA, Eclipse, or VS Code as a Maven project and run it from the IDE. Make sure the IDE is configured to use a compatible JDK and that JavaFX is available in the project classpath or configured via the plugin.


---

## Contributing

Contributions are welcome. Please open issues or pull requests on the repository for bug reports or enhancements.
