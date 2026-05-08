# PixelLab

Interactive desktop app for exploring images, color spaces, and image
processing in real time.

> Course: Multimedia 2026 · Faculty of Informatics Engineering

## Features

- Open images via toolbar, drag-and-drop onto the canvas, or the recent files list.
- Convert the canvas display between **RGB / CMYK / HSV / YUV / LAB / YCbCr**.
- Per-channel **offset slider + on/off toggle + grayscale thumbnail** in the right panel.
- **Color quantization** slider (2–256 colors) using the median-cut algorithm.
- **3D color-space visualization** (Image / 3D Color Space tab) with mouse drag, scroll/+−
  zoom, click-to-pick, and a synchronized panel showing the picked color in all six systems
  with copy-to-clipboard buttons.
- **Save / Save As / Reset** with PNG / JPEG / BMP support.
- Persistent **recent files** list at `~/.pixellab/recent.json`.
- Background processing pipeline keeps the UI responsive on large images;
  spinner appears for ops > 200 ms.
- Keyboard shortcuts: **Ctrl+O** (Open), **Ctrl+S** (Save), **Ctrl+Shift+S** (Save As), **Ctrl+R** (Reset).

## Tech Stack

| Concern | Choice |
|---|---|
| Language | Java 26 |
| UI Toolkit | JavaFX 26 |
| Build | Maven |
| JSON | Jackson |
| Testing | JUnit 5 |

## Architecture

Clean Architecture + MVVM. Dependencies flow inward only — see [PROJECT_PLAN.md](PROJECT_PLAN.md)
for the full breakdown. Manual DI from [`AppComponent`](src/main/java/com/alaishat/mohammad/pixellab/AppComponent.java).

## Build & Run

Requires **JDK 26** and **Maven 3.9+**.

### From the command line

```bash
# Run from source (recommended for development)
mvn javafx:run

# Run the test suite (color space conversions: known values + round-trips)
mvn test

# Build a runnable fat jar
mvn package
java -jar target/PixelLab-1.0-SNAPSHOT-fat.jar
```

The fat jar bundles JavaFX natives for the **build platform** — copy it to a
machine of the same OS/arch and `java -jar …` will work.

### From IntelliJ IDEA

Run the [`Launcher`](src/main/java/com/alaishat/mohammad/pixellab/Launcher.java)
class (not `App`) — this avoids the *"JavaFX runtime components are missing"*
error that the JVM throws when an `Application`-extending main class is run with
JavaFX on the classpath. Alternatively, use the IntelliJ Maven panel →
*Plugins → javafx → javafx:run*.

## Project Layout

```
src/main/java/com/alaishat/mohammad/pixellab/
├── App.java               JavaFX Application entry
├── Launcher.java          Plain entry point (IDE / fat jar)
├── AppComponent.java      Manual DI composition root
├── domain/                Pure logic — no JavaFX
│   ├── image/             PixelBuffer, ImageMetadata, EditSession, ports
│   ├── color/             ColorSpace, ColorTriplet, Cmyk, conversions/
│   └── recentfiles/       RecentFile + store port
├── features/              One package per feature (usecase / viewmodel / view)
│   ├── imageworkspace/
│   ├── recentfiles/
│   ├── editsession/
│   ├── colorspace/
│   ├── channels/
│   ├── quantization/
│   ├── visualization3d/
│   └── colorpicker/
├── infrastructure/        File I/O + JSON store impls
└── shared/threading/      BackgroundExecutor + UpdateCoalescer
```

Tests mirror the main tree under `src/test/java/`.
