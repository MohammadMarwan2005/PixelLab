# PixelLab — A Complete Reading Guide

This document walks you through the entire codebase from the ground up. Start at
section 1 and read linearly — each section builds on the previous one.

---

## Table of Contents

1. [What the app does](#1-what-the-app-does)
2. [The pixel: the smallest unit](#2-the-pixel-the-smallest-unit)
3. [PixelBuffer: holding an entire image in memory](#3-pixelbuffer-holding-an-entire-image-in-memory)
4. [Color spaces and conversions](#4-color-spaces-and-conversions)
5. [EditSession: the loaded image and its edits](#5-editsession-the-loaded-image-and-its-edits)
6. [Ports: interfaces between layers](#6-ports-interfaces-between-layers)
7. [Infrastructure: reading and writing files](#7-infrastructure-reading-and-writing-files)
8. [Threading model: keeping the UI alive](#8-threading-model-keeping-the-ui-alive)
9. [Architecture overview: Clean Architecture + MVVM](#9-architecture-overview-clean-architecture--mvvm)
10. [AppComponent: the wiring room](#10-appcomponent-the-wiring-room)
11. [Feature walkthrough](#11-feature-walkthrough)
    - [ImageWorkspace — loading an image](#111-imageworkspace--loading-an-image)
    - [ColorSpace — what the canvas displays](#112-colorspace--what-the-canvas-displays)
    - [Channels — adjusting individual components](#113-channels--adjusting-individual-components)
    - [Quantization — reducing the color palette](#114-quantization--reducing-the-color-palette)
    - [EditSession — save and reset](#115-editsession--save-and-reset)
    - [3D Visualization — seeing the color space as a cloud](#116-3d-visualization--seeing-the-color-space-as-a-cloud)
    - [ColorPicker — picking a color from the 3D scene](#117-colorpicker--picking-a-color-from-the-3d-scene)
    - [RecentFiles — remembering previously opened images](#118-recentfiles--remembering-previously-opened-images)
12. [The processing pipeline end-to-end](#12-the-processing-pipeline-end-to-end)
13. [The UI layout](#13-the-ui-layout)
14. [Entry points](#14-entry-points)
15. [Tests](#15-tests)

---

## 1. What the app does

PixelLab is a desktop image-processing workbench built with Java + JavaFX. You
open an image, and the app lets you:

- **View** the image rendered in six different color spaces (RGB, CMYK, HSV,
  YUV, LAB, YCbCr).
- **Adjust** individual channels of those spaces (e.g., shift the Hue channel
  in HSV) with a slider, and toggle channels on or off. The image updates live.
- **Quantize** the palette down to as few as 1 color using the median-cut
  algorithm.
- **Visualize** the color distribution of the image as a 3D point cloud,
  rotatable with the mouse, in the currently selected color space.
- **Pick** a color from the 3D cloud and inspect its value in all six color
  systems simultaneously.
- **Save / Save As / Reset** the edited image.
- **Quickly re-open** files from a persistent recent-files list.

---

## 2. The pixel: the smallest unit

**File:** [domain/image/Pixel.java](src/main/java/com/alaishat/mohammad/pixellab/domain/image/Pixel.java)

A pixel is four 8-bit channels: alpha, red, green, blue — each in `[0, 255]`.

```
record Pixel(int alpha, int red, int green, int blue)
```

The two most important methods are:

```java
int   toArgb()          // packs into a single 32-bit int: AARRGGBB
Pixel fromArgb(int argb) // unpacks back to four fields
```

The bit layout for the packed int is:

```
bits 31..24  alpha
bits 23..16  red
bits 15..8   green
bits 7..0    blue
```

`Pixel` is a Java `record` — immutable, value-typed, used when you want to
inspect one pixel at a time. For bulk work (iterating over all pixels) the code
works directly with the packed `int`, not with `Pixel` objects, because boxing
would be prohibitively slow.

---

## 3. PixelBuffer: holding an entire image in memory

**File:** [domain/image/PixelBuffer.java](src/main/java/com/alaishat/mohammad/pixellab/domain/image/PixelBuffer.java)

`PixelBuffer` is a `width × height` grid of ARGB pixels stored as a flat
`int[]` in **row-major order**:

```
index = y * width + x
```

The array is exposed directly via `data()` — callers may mutate it in place.
This is intentional: use cases that process millions of pixels cannot afford the
cost of copying per access.

Key operations:

| Method | Purpose |
|---|---|
| `data()` | Direct reference to the backing `int[]` |
| `getArgb(x, y)` / `setArgb(x, y, v)` | Indexed access with bounds check |
| `getPixel(x, y)` / `setPixel(x, y, p)` | Same but with `Pixel` objects |
| `copy()` | Deep copy — returns a new independent buffer |
| `copyFrom(src)` | In-place copy from another buffer of the same size |

> **Why a mutable int[] instead of an immutable structure?**
> Image processing loops touch every single pixel. A 1920×1080 image has ~2 M
> pixels. Creating a new Java object per pixel would generate ~2 M objects per
> frame. The flat int[] avoids GC pressure entirely.

---

## 4. Color spaces and conversions

### 4.1 ColorSpace enum

**File:** [domain/color/ColorSpace.java](src/main/java/com/alaishat/mohammad/pixellab/domain/color/ColorSpace.java)

The six supported color spaces are listed as enum constants. Each carries its
display name and the labels for its channels (e.g., `"H"`, `"S"`, `"V"` for HSV).

```java
RGB, CMYK, HSV, YUV, LAB, YCBCR
```

CMYK has four components; all others have three. The enum exposes
`componentCount()` and `componentLabel(index)` so the UI can build channel
controls dynamically without hard-coded `if/else` chains.

### 4.2 Value types: ColorTriplet and Cmyk

**Files:** [domain/color/ColorTriplet.java](src/main/java/com/alaishat/mohammad/pixellab/domain/color/ColorTriplet.java),
[domain/color/Cmyk.java](src/main/java/com/alaishat/mohammad/pixellab/domain/color/Cmyk.java)

```java
record ColorTriplet(double a, double b, double c)
record Cmyk(double c, double m, double y, double k)
```

These are simple value carriers. The fields are named `a/b/c` (generic) because
the same `ColorTriplet` type is re-used for RGB, HSV, YUV, LAB, and YCbCr — the
meaning of each component depends on which conversion class produced it.

Ranges vary by space:
- RGB: `r, g, b ∈ [0, 1]`
- HSV: `H ∈ [0, 360)`, `S, V ∈ [0, 1]`
- LAB: `L ∈ [0, 100]`, `a, b ≈ [-128, 127]`
- CMYK: all four components in `[0, 1]`

### 4.3 Conversion classes

**Package:** [domain/color/conversion/](src/main/java/com/alaishat/mohammad/pixellab/domain/color/conversion/)

Five stateless utility classes, one per non-RGB space:

| Class | Converts |
|---|---|
| `RgbHsv` | RGB ↔ HSV |
| `RgbCmyk` | RGB ↔ CMYK |
| `RgbYuv` | RGB ↔ YUV |
| `RgbYCbCr` | RGB ↔ YCbCr |
| `RgbLab` | RGB ↔ CIE LAB (via XYZ D65) |

All have the same shape: two static methods, `toXxx(ColorTriplet rgb)` and
`toRgb(ColorTriplet xxx)`. They operate on single pixels, returning a new value.
Bulk per-pixel work (e.g., converting a whole buffer) is done by the use-case
layer calling these in a loop.

---

## 5. EditSession: the loaded image and its edits

**File:** [domain/image/EditSession.java](src/main/java/com/alaishat/mohammad/pixellab/domain/image/EditSession.java)

When you open an image, the app creates an `EditSession`. It holds:

```
originalBuffer   — immutable copy of the pixels as loaded from disk
workingBuffer    — what use cases modify; the canvas always shows this
sourcePath       — the file it came from (needed for Save)
originalFormat   — "PNG" / "JPEG" / "BMP" (needed for Save to preserve format)
```

The design rule is: **`originalBuffer` is never written to**. It is the
permanent source-of-truth for `Reset`. All processing (channel adjustments,
quantization) produces a new `PixelBuffer` and calls `replaceWorking()` to
install it.

```java
session.replaceWorking(newBuffer);
```

---

## 6. Ports: interfaces between layers

**Files:**
[domain/image/ImageLoader.java](src/main/java/com/alaishat/mohammad/pixellab/domain/image/ImageLoader.java),
[domain/image/ImageSaver.java](src/main/java/com/alaishat/mohammad/pixellab/domain/image/ImageSaver.java),
[domain/recentfiles/RecentFilesStore.java](src/main/java/com/alaishat/mohammad/pixellab/domain/recentfiles/RecentFilesStore.java)

The domain layer defines **interfaces** for anything that touches the outside
world. Use cases depend on these interfaces, never on the concrete
implementations:

- `ImageLoader.load(Path) → LoadedImage` — reads pixels and metadata from disk.
- `ImageSaver.save(PixelBuffer, Path, String format)` — writes pixels to disk.
- `RecentFilesStore.load() / add() / remove()` — persists the recent-files list.

This separation means you could swap `FileSystemImageLoader` for a network
loader and none of the use-case or view-model code would change.

---

## 7. Infrastructure: reading and writing files

**Package:** [infrastructure/](src/main/java/com/alaishat/mohammad/pixellab/infrastructure/)

### FileSystemImageLoader

**File:** [infrastructure/io/FileSystemImageLoader.java](src/main/java/com/alaishat/mohammad/pixellab/infrastructure/io/FileSystemImageLoader.java)

Uses Java's `javax.imageio.ImageIO` to read a file. The key step is:

```java
BufferedImage image = reader.read(0);
int[] argb = new int[w * h];
image.getRGB(0, 0, w, h, argb, 0, w);
```

`BufferedImage.getRGB()` always delivers packed ARGB regardless of the source
format, so PNG, JPEG, and BMP all come out the same way. The result is wrapped
in a `PixelBuffer`.

### FileSystemImageSaver

**File:** [infrastructure/io/FileSystemImageSaver.java](src/main/java/com/alaishat/mohammad/pixellab/infrastructure/io/FileSystemImageSaver.java)

The reverse: wraps the `PixelBuffer` into a `BufferedImage` and calls
`ImageIO.write()`.

### JsonRecentFilesStore

**File:** [infrastructure/persistence/JsonRecentFilesStore.java](src/main/java/com/alaishat/mohammad/pixellab/infrastructure/persistence/JsonRecentFilesStore.java)

Reads and writes `~/.pixellab/recent.json` using Jackson. Each entry is a
`RecentFile` (path + last-opened timestamp).

---

## 8. Threading model: keeping the UI alive

**Package:** [shared/threading/](src/main/java/com/alaishat/mohammad/pixellab/shared/threading/)

Image processing is slow. On a large image, converting every pixel to LAB and
back takes tens to hundreds of milliseconds. If that ran on the JavaFX
Application Thread (the UI thread), the window would freeze on every slider
move. PixelLab solves this with two classes.

### BackgroundExecutor

**File:** [shared/threading/BackgroundExecutor.java](src/main/java/com/alaishat/mohammad/pixellab/shared/threading/BackgroundExecutor.java)

A single background thread with a **keyed task queue**. Submit a task with a
key; if a task with the same key is already waiting, it gets *replaced*. This
means only the latest slider position is ever processed — intermediate values
are silently dropped.

```java
executor.submit("channels:recompute", () -> { /* heavy pixel work */ });
```

Different keys coexist, so the channels stage and the quantization stage can
both have pending work without clobbering each other.

A `busyProperty()` (JavaFX `BooleanProperty`) flips to `true` while any task is
running and `false` when the queue drains. The toolbar binds a progress spinner
to this property — the spinner appears when ops take more than ~200 ms.

### UpdateCoalescer

**File:** [shared/threading/UpdateCoalescer.java](src/main/java/com/alaishat/mohammad/pixellab/shared/threading/UpdateCoalescer.java)

A thin helper that encodes the pattern all view models use:

```java
coalescer.submit(key,
    () -> computeOnBackgroundThread(),   // Supplier<T> — no JavaFX access
    result -> publishOnUIThread(result)  // Consumer<T> — runs via Platform.runLater
);
```

The `compute` lambda runs on the background thread and must not touch JavaFX
properties. The `publish` lambda receives the result and runs on the JavaFX
Application Thread, so it can safely write to properties that views observe.

---

## 9. Architecture overview: Clean Architecture + MVVM

The project follows **Clean Architecture** layered inside **MVVM**:

```
┌──────────────────────────────────────────────────────────────┐
│  domain/                                                      │
│  ─ Pure Java, zero JavaFX, zero I/O                           │
│  ─ PixelBuffer, EditSession, ColorSpace, conversions, ports   │
└───────────────────────────┬──────────────────────────────────┘
                            │  depends on
┌───────────────────────────▼──────────────────────────────────┐
│  features/  (one package per feature)                         │
│  ├── usecase/    — orchestration logic (calls domain + ports) │
│  ├── viewmodel/  — JavaFX properties + state                  │
│  └── view/       — JavaFX nodes (FXML-free, code-only)        │
└───────────────────────────┬──────────────────────────────────┘
                            │  depends on
┌───────────────────────────▼──────────────────────────────────┐
│  infrastructure/                                              │
│  ─ FileSystemImageLoader, FileSystemImageSaver                │
│  ─ JsonRecentFilesStore                                       │
└──────────────────────────────────────────────────────────────┘
```

**Dependency rule:** arrows point inward only. `domain` knows nothing about
JavaFX, files, or features. `features` knows `domain` but not `infrastructure`.
`AppComponent` (the composition root) is the only place that references all
layers simultaneously.

**MVVM:** Each feature has:
- A **view model** that holds observable `Property<T>` fields and methods the
  view calls (e.g., `open(path)`). It knows nothing about buttons or layouts.
- A **view** (a JavaFX `Node` subclass) that binds to the view model's
  properties. It never calls use cases directly.
- One or more **use cases** (plain Java classes with an `execute()` method)
  that contain the actual business logic.

---

## 10. AppComponent: the wiring room

**File:** [AppComponent.java](src/main/java/com/alaishat/mohammad/pixellab/AppComponent.java)

This is the **manual dependency injection** container. Every view model and use
case is instantiated here, in dependency order. Reading the constructor top to
bottom tells you the entire dependency graph:

```
BackgroundExecutor
  └── UpdateCoalescer
        ├── ImageWorkspaceViewModel  ← LoadImageUseCase ← FileSystemImageLoader
        │     └── ColorSpaceViewModel  ← ConvertColorSpaceUseCase
        │           └── ChannelsViewModel  ← ApplyChannelAdjustmentsUseCase
        │                                  ← SplitChannelsUseCase
        │                 └── QuantizationViewModel  ← QuantizeColorsUseCase
        ├── ColorSpaceVisualizationViewModel  ← SampleColorSpaceUseCase
        │     └── ColorPickerViewModel  ← CopyToClipboardUseCase
        ├── EditSessionViewModel  ← ResetUseCase, SaveImageUseCase, SaveAsImageUseCase
        └── RecentFilesViewModel  ← JsonRecentFilesStore
```

Two cross-feature wires are set up here as well:
1. When a new image is loaded (`currentSourceProperty` changes),
   `RecentFilesViewModel.recordOpened()` is called automatically — keeping
   `ImageWorkspaceViewModel` ignorant of the recents feature.
2. `recentFilesViewModel.refresh()` is called once at startup to populate the
   list from disk.

`AppComponent.shutdown()` is called when the app closes and stops the background
thread cleanly.

---

## 11. Feature walkthrough

### 11.1 ImageWorkspace — loading an image

**View model:** [features/imageworkspace/viewmodel/ImageWorkspaceViewModel.java](src/main/java/com/alaishat/mohammad/pixellab/features/imageworkspace/viewmodel/ImageWorkspaceViewModel.java)

This is the **central source of truth** for the loaded image. All other view
models observe its properties.

Key properties:

| Property | Type | Meaning |
|---|---|---|
| `editSession` | `ObjectProperty<EditSession>` | The loaded image and its working copy |
| `currentBuffer` | `ObjectProperty<PixelBuffer>` | What the canvas paints — written by `ColorSpaceViewModel` |
| `currentMetadata` | `ObjectProperty<ImageMetadata>` | Filename, format, size, dimensions |
| `currentSource` | `ObjectProperty<Path>` | File path — changing this triggers the recents feature |
| `workingBufferRevision` | `ReadOnlyIntegerProperty` | Tick counter; bumped when `workingBuffer` changes in-place |

When `open(path)` is called:
1. `LoadImageUseCase.execute(path)` delegates to `FileSystemImageLoader`.
2. An `EditSession` is created from the loaded pixels.
3. The properties are set in order: `editSession` → `currentMetadata` → `currentSource`.
4. `ColorSpaceViewModel` observes `editSession` and immediately recomputes `currentBuffer`.

The `workingBufferRevision` counter solves a subtle problem: JavaFX properties
only fire change events when the **reference** changes. If a use case mutates the
`workingBuffer` in place, `currentBuffer` still points to the same object and
will not fire. Bumping the revision counter provides an explicit re-render signal.

### 11.2 ColorSpace — what the canvas displays

**View model:** [features/colorspace/viewmodel/ColorSpaceViewModel.java](src/main/java/com/alaishat/mohammad/pixellab/features/colorspace/viewmodel/ColorSpaceViewModel.java)

**Use case:** [features/colorspace/usecase/ConvertColorSpaceUseCase.java](src/main/java/com/alaishat/mohammad/pixellab/features/colorspace/usecase/ConvertColorSpaceUseCase.java)

This view model **owns all writes to `currentBufferProperty()`** — it is the
only code that decides what the canvas paints.

Whenever the selected color space, the loaded session, or the working buffer
revision changes, `recomputeDisplay()` runs:

- If the selected space is **RGB**: the working buffer is published directly (no
  conversion needed, no background hop).
- Otherwise: `ConvertColorSpaceUseCase.execute(workingBuffer, targetSpace)` is
  submitted to the `UpdateCoalescer` under the key `"colorspace:display"`. When
  it completes, `currentBufferProperty` is updated on the UI thread.

**What `ConvertColorSpaceUseCase` does:**  
For each pixel, it unpacks the ARGB int, normalizes the RGB channels to `[0, 1]`,
calls the appropriate conversion function, then **re-scales the result back to
0–255 and packs it into RGB slots of the output**. The result is not the "true"
color — it is a visual encoding that lets you see the channel intensities as
color. For example, in HSV mode: the red channel of the output encodes Hue, the
green encodes Saturation, and the blue encodes Value.

### 11.3 Channels — adjusting individual components

**View model:** [features/channels/viewmodel/ChannelsViewModel.java](src/main/java/com/alaishat/mohammad/pixellab/features/channels/viewmodel/ChannelsViewModel.java)

**Use case:** [features/channels/usecase/ApplyChannelAdjustmentsUseCase.java](src/main/java/com/alaishat/mohammad/pixellab/features/channels/usecase/ApplyChannelAdjustmentsUseCase.java)

The right panel shows one slider + toggle per channel of the currently selected
color space. `ChannelsViewModel` holds a `ChannelControl` object for each:

```java
class ChannelControl {
    int index;
    String label;          // "R", "H", "Y", etc.
    DoubleProperty offset; // slider value in [-1, +1]
    BooleanProperty enabled;
    ObjectProperty<PixelBuffer> thumbnail; // grayscale preview strip
}
```

When the selected color space changes, `rebuildChannels()` discards all existing
controls and creates new ones matching the new space's component count. This is
why switching from HSV (3 channels) to CMYK (4 channels) automatically shows a
fourth slider.

**How a slider drag works:**

1. The `offset` property changes.
2. A listener calls `recompute()`.
3. `recompute()` builds a `ChannelAdjustment[]` from the current slider values.
4. It submits a task to the `UpdateCoalescer` under key `"channels:recompute"`.
   - If you drag quickly, intermediate values are dropped; only the latest fires.
5. On the background thread: `ApplyChannelAdjustmentsUseCase.execute()` is called.

**How `ApplyChannelAdjustmentsUseCase` works:**  
For each pixel in the **original buffer** (never the working buffer — to avoid
compounding offsets), it:
1. Unpacks RGB.
2. Calls `ChannelCodec.decompose(space, r, g, b)` — which converts the pixel to
   the target color space and returns a `double[]` of components.
3. Applies each `ChannelAdjustment` (offset / multiply / zero if disabled).
4. Calls `ChannelCodec.recomposeRgb(space, channels)` — converts back to RGB.
5. Clamps to `[0, 255]` and packs the result.

The output is a new `PixelBuffer` in **RGB space** — the working state is always
kept in RGB. Only the canvas display converts to other spaces.

6. The result is published as `channelAdjustedBuffer` — the downstream pipeline
   stage (`QuantizationViewModel`) observes this.

### 11.4 Quantization — reducing the color palette

**View model:** [features/quantization/viewmodel/QuantizationViewModel.java](src/main/java/com/alaishat/mohammad/pixellab/features/quantization/viewmodel/QuantizationViewModel.java)

**Use case:** [features/quantization/usecase/QuantizeColorsUseCase.java](src/main/java/com/alaishat/mohammad/pixellab/features/quantization/usecase/QuantizeColorsUseCase.java)

This is the **final stage** of the processing pipeline. It receives the
channel-adjusted buffer (from `ChannelsViewModel`) and applies color quantization.

When N = 256 (the slider maximum), the buffer is passed through unchanged.
Otherwise, `QuantizeColorsUseCase.execute(buffer, N)` runs the **median-cut
algorithm**:

1. All pixels start in a single "bucket".
2. Repeat until there are N buckets:
   - Find the bucket with the largest color range along any R, G, or B axis.
   - Sort that bucket's pixels by that axis.
   - Split it at the median.
3. Replace every pixel with the average color of its bucket.

Alpha is preserved — only the RGB triple is quantized.

After quantization, the result is installed as the session's working buffer:

```java
session.replaceWorking(result);
workspace.republishWorkingBuffer();  // bumps the revision counter
```

Bumping the revision causes `ColorSpaceViewModel` to recompute the display
buffer, which updates the canvas.

### 11.5 EditSession — save and reset

**View model:** [features/editsession/viewmodel/EditSessionViewModel.java](src/main/java/com/alaishat/mohammad/pixellab/features/editsession/viewmodel/EditSessionViewModel.java)

Handles the toolbar's Save, Save As, and Reset buttons.

- **Save:** calls `SaveImageUseCase.execute(session)`, which writes
  `session.workingBuffer()` to `session.sourcePath()` in `session.originalFormat()`.
- **Save As:** opens a file dialog (in the view) and calls `SaveAsImageUseCase`.
- **Reset:** calls `ResetUseCase.execute(session)` (which copies `originalBuffer`
  back into `workingBuffer`), then calls `channelsViewModel.resetAll()` and
  `quantizationViewModel.resetAll()` to zero out all slider state. The pipeline
  then recomputes from the fresh original.

### 11.6 3D Visualization — seeing the color space as a cloud

**View model:** [features/visualization3d/viewmodel/ColorSpaceVisualizationViewModel.java](src/main/java/com/alaishat/mohammad/pixellab/features/visualization3d/viewmodel/ColorSpaceVisualizationViewModel.java)

**View:** [features/visualization3d/view/ColorSpaceVisualizationView.java](src/main/java/com/alaishat/mohammad/pixellab/features/visualization3d/view/ColorSpaceVisualizationView.java)

When the selected color space changes, `SampleColorSpaceUseCase.execute(space)`
generates a fixed grid of `ColorSample` objects — each has a **3D position** in
the unit cube and an **RGB color** (so each sphere is painted with its actual
color).

The view renders each sample as a small `Sphere` inside a JavaFX `SubScene`
(a 3D-capable subscene embedded in the 2D layout). The `SubScene` uses a
`PerspectiveCamera` and `PhongMaterial` for lighting.

Interaction is installed directly on the `SubScene`:
- **Mouse drag** — updates `rotateY` and `rotateX` transforms on the sample group.
- **Scroll** — adjusts `camera.translateZ`.
- **Click on a sphere** — sets `pickedSampleProperty` on the view model.

### 11.7 ColorPicker — picking a color from the 3D scene

**View model:** [features/colorpicker/viewmodel/ColorPickerViewModel.java](src/main/java/com/alaishat/mohammad/pixellab/features/colorpicker/viewmodel/ColorPickerViewModel.java)

Observes `ColorSpaceVisualizationViewModel.pickedSampleProperty()`. When a
sphere is clicked, the picked `ColorSample` contains the RGB triple of that
color. The view model converts it to all six color spaces and exposes the
values as formatted strings for the UI to display. Copy-to-clipboard buttons
call `CopyToClipboardUseCase`.

### 11.8 RecentFiles — remembering previously opened images

**View model:** [features/recentfiles/viewmodel/RecentFilesViewModel.java](src/main/java/com/alaishat/mohammad/pixellab/features/recentfiles/viewmodel/RecentFilesViewModel.java)

Holds an `ObservableList<RecentFile>`. On startup, `refresh()` loads entries
from `~/.pixellab/recent.json`. In `AppComponent`, a listener on
`ImageWorkspaceViewModel.currentSourceProperty()` calls
`recentFilesViewModel.recordOpened(path)` automatically whenever an image loads
— the recents list stays in sync without any view-model being aware of the other.

Clicking a recent file in the left panel calls `workspace.open(path)` directly.

---

## 12. The processing pipeline end-to-end

Every time something changes — a new image, a slider move, a color space switch,
a quantization step — the same pipeline runs:

```
original buffer (in EditSession)
        │
        ▼
ChannelsViewModel  ── recompute() on BackgroundExecutor ──►  channelAdjustedBuffer
        │
        ▼
QuantizationViewModel  ── recompute() on BackgroundExecutor
        │
        ├── quantize.execute(channelAdjustedBuffer, N)
        │
        └── session.replaceWorking(result)
            workspace.republishWorkingBuffer()   ← bumps revision counter
                    │
                    ▼
        ColorSpaceViewModel  ── recomputeDisplay() on BackgroundExecutor
                    │
                    ├── (RGB) publish working buffer directly
                    └── (non-RGB) ConvertColorSpaceUseCase → currentBuffer
                                        │
                                        ▼
                              ImageCanvasView (canvas paints currentBuffer)
```

The pipeline is **reactive**: each stage is triggered by an observable property
change, not by explicit method calls. Changing the color space triggers
`ColorSpaceViewModel` → re-renders the canvas. Dragging a channel slider triggers
`ChannelsViewModel` → `QuantizationViewModel` → `ColorSpaceViewModel` → canvas.

**Stale-result guard:** every stage snapshot its inputs before the background
hop. When it publishes, it checks whether the session, space, or source buffer
are still the same. If they changed (e.g., the user opened a new image while the
computation was running), the result is silently discarded.

---

## 13. The UI layout

**File:** [features/imageworkspace/view/MainWindowView.java](src/main/java/com/alaishat/mohammad/pixellab/features/imageworkspace/view/MainWindowView.java)

`MainWindowView` extends `BorderPane` (a JavaFX layout with five zones) and
places one view into each zone:

```
┌─────────────────────────────────────────────────────────┐
│  ToolbarView  (top)                                      │
│  Open / Save / Save As / Reset / color-space selector    │
├──────────────┬──────────────────────────┬───────────────┤
│ LeftPaneView │    CenterPaneView        │ RightPaneView  │
│              │  ┌──────────────────┐   │               │
│ Color space  │  │  Tab: Image      │   │ Metadata panel │
│ selector     │  │  ImageCanvasView │   │ Channel sliders│
│              │  ├──────────────────┤   │ Thumbnails     │
│ Recent files │  │  Tab: 3D Space   │   │ Quantization   │
│ list         │  │  VisualizationView│  │ slider         │
│              │  │  ColorPickerView  │  │               │
│              │  └──────────────────┘   │               │
└──────────────┴──────────────────────────┴───────────────┘
```

All four pane views receive the view models they need through their constructor
(no global state, no singletons). Each view binds directly to the view model
properties and updates automatically when they change.

Error handling is centralized in `MainWindowView`: it observes
`workspace.lastErrorProperty()` and `edit.lastErrorProperty()` and shows an
`Alert` dialog when either is set.

---

## 14. Entry points

**File:** [Launcher.java](src/main/java/com/alaishat/mohammad/pixellab/Launcher.java)

A plain `main` class with one line: `App.main(args)`. This exists because the
JVM throws *"JavaFX runtime components are missing"* when the class with
`main()` directly extends `Application` and JavaFX is on the classpath rather
than the module path. `Launcher` is the actual entry point in the fat-jar
manifest and in IntelliJ's run configuration.

**File:** [App.java](src/main/java/com/alaishat/mohammad/pixellab/App.java)

The JavaFX `Application` subclass. Its three lifecycle methods map to three
events:

| Method | When | What it does |
|---|---|---|
| `init()` | Before the UI thread starts | Creates `AppComponent` — builds all view models and use cases |
| `start(stage)` | On the UI thread | Creates `MainWindowView`, wraps it in a `Scene`, shows the window |
| `stop()` | When the window closes | Calls `AppComponent.shutdown()` — stops the background thread |

---

## 15. Tests

**Package:** [src/test/java/.../domain/color/conversion/](src/test/java/com/alaishat/mohammad/pixellab/domain/color/conversion/)

Tests cover the five color space conversion classes. Each has two test categories:

1. **Known-value tests:** convert a specific RGB color and assert the expected
   output in the target space (e.g., `(1, 0, 0)` → HSV `(0°, 1, 1)`).
2. **Round-trip tests:** convert from RGB to the target space and back; assert
   that the result matches the input within a small epsilon (floating-point
   tolerance).

Test data is shared via `ColorSamples.java` — a list of representative RGB
colors (red, green, blue, white, black, gray, and arbitrary mid-tones).

---

## Summary: reading order for the code

If you want to read the source files in logical order rather than alphabetically:

1. `domain/image/Pixel.java` — smallest unit
2. `domain/image/PixelBuffer.java` — image container
3. `domain/color/ColorSpace.java`, `ColorTriplet.java`, `Cmyk.java` — color types
4. `domain/color/conversion/RgbHsv.java` (pick any one) — conversion math
5. `domain/image/EditSession.java` — edit lifecycle
6. `domain/image/ImageLoader.java` (port interface) — boundary between layers
7. `infrastructure/io/FileSystemImageLoader.java` — concrete implementation
8. `shared/threading/BackgroundExecutor.java` — threading primitive
9. `shared/threading/UpdateCoalescer.java` — the pattern every view model uses
10. `AppComponent.java` — see all dependencies in one place
11. `features/imageworkspace/viewmodel/ImageWorkspaceViewModel.java` — central hub
12. `features/colorspace/viewmodel/ColorSpaceViewModel.java` — first pipeline stage
13. `features/channels/viewmodel/ChannelsViewModel.java` — second pipeline stage
14. `features/quantization/viewmodel/QuantizationViewModel.java` — third and final stage
15. `features/imageworkspace/view/MainWindowView.java` — how it all appears on screen
16. `App.java` + `AppComponent.java` — startup and wiring
