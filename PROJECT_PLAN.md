# PixelLab — Project Plan

> Interactive desktop app for exploring images, color spaces, and image processing in real time.
> Course: Multimedia 2026 · Faculty of Informatics Engineering

---

## 1. Philosophy

The app is graded on **how well direct visual interaction simplifies color-space concepts**. Every architectural decision should serve that goal: real-time feedback, clean UI, and visualizations that make math feel tangible.

Three rules we will not break:

1. **The original file on disk is never touched until the user clicks Save.** Editing happens entirely in memory.
2. **The UI thread is never blocked.** Per-pixel work runs in the background; results are posted back to the JavaFX Application Thread.
3. **Domain and use cases never import JavaFX.** UI is replaceable; logic is not.

---

## 2. Tech Stack

| Concern | Choice          |
|---|-----------------|
| Language | Java 26 (LTS)   |
| UI Toolkit | JavaFX 26       |
| Build | Maven           |
| JSON | Jackson         |
| Testing | JUnit 5         |
| Logging (optional) | SLF4J + Logback |

---

## 3. Architecture

**Clean Architecture + MVVM for the UI layer.** Dependencies flow inward only.

```
┌──────────────────────────────────────────────┐
│  View Layer  (JavaFX nodes / FXML)            │
│   - Pure UI. Binds to ViewModel properties.   │
└─────────────────────▲────────────────────────┘
                      │ binds
┌─────────────────────▼────────────────────────┐
│  ViewModel Layer                              │
│   - Observable state.                         │
│   - Calls use cases. Holds no business logic. │
└─────────────────────▲────────────────────────┘
                      │ calls
┌─────────────────────▼────────────────────────┐
│  Use Case Layer                               │
│   - One class per business operation.         │
│   - Pure, testable, no UI imports.            │
└─────────────────────▲────────────────────────┘
                      │ uses
┌─────────────────────▼────────────────────────┐
│  Domain Layer                                 │
│   - PixelBuffer, ColorSpace, conversion math. │
│   - Zero external dependencies.               │
└─────────────────────▲────────────────────────┘
                      │ implements
┌─────────────────────▼────────────────────────┐
│  Infrastructure Layer                         │
│   - File I/O, JSON store. Behind interfaces.  │
└──────────────────────────────────────────────┘
```

**Manual DI** from a single composition root (`AppComponent.java`). No Spring, no Guice. Reading `main` should reveal the entire dependency graph.

**Feature-based packaging.** Each feature owns its own `usecase/`, `viewmodel/`, `view/`.

---

## 4. Folder Structure

```
pixellab/
├── pom.xml
├── README.md
├── PROJECT_PLAN.md
└── src/
    ├── main/
    │   ├── java/com/pixellab/
    │   │   ├── App.java                    ← JavaFX Application entry
    │   │   ├── AppComponent.java           ← composition root (manual DI)
    │   │   │
    │   │   ├── domain/
    │   │   │   ├── image/
    │   │   │   │   ├── PixelBuffer.java        ← mutable, raw int[] for speed
    │   │   │   │   ├── ImageMetadata.java      ← record
    │   │   │   │   └── Pixel.java
    │   │   │   └── color/
    │   │   │       ├── ColorSpace.java         ← enum: RGB, CMYK, HSV, YUV, LAB, YCbCr
    │   │   │       ├── ColorTriplet.java       ← record
    │   │   │       └── conversion/             ← pure conversion math, one class per pair
    │   │   │
    │   │   ├── features/
    │   │   │   ├── imageworkspace/
    │   │   │   │   ├── usecase/
    │   │   │   │   ├── viewmodel/
    │   │   │   │   └── view/
    │   │   │   ├── recentfiles/
    │   │   │   ├── editsession/
    │   │   │   ├── colorspace/
    │   │   │   ├── channels/
    │   │   │   ├── quantization/
    │   │   │   ├── visualization3d/
    │   │   │   └── colorpicker/
    │   │   │
    │   │   ├── infrastructure/
    │   │   │   ├── io/
    │   │   │   │   ├── FileSystemImageLoader.java
    │   │   │   │   └── FileSystemImageSaver.java
    │   │   │   └── persistence/
    │   │   │       └── JsonRecentFilesStore.java
    │   │   │
    │   │   └── shared/
    │   │       ├── threading/
    │   │       │   ├── BackgroundExecutor.java
    │   │       │   └── UpdateCoalescer.java
    │   │       └── ui/                          ← shared UI components
    │   │
    │   └── resources/
    │       ├── fxml/
    │       └── styles/
    │
    └── test/
        └── java/com/pixellab/                   ← mirrors main structure
```

---

## 5. Conventions

- **Use case classes:** `<Verb><Noun>UseCase`, single `execute(input)` method.
- **ViewModels:** `<Feature>ViewModel`. Expose JavaFX `ObservableValue`s only — no `Node`s.
- **Records** for value objects (`ImageMetadata`, `ColorTriplet`, `RecentFile`).
- **`PixelBuffer` is mutable** for performance — it's the one exception to immutability.
- **Use case interfaces** live next to their implementations unless multiple implementations exist.
- **Threading:** any use case that loops over pixels runs through `BackgroundExecutor`. UI updates marshalled back via `Platform.runLater`.

---

## 6. Implementation Phases

Each step is small enough to complete in a single Claude Code session. Check items off as you go.

### Phase 0 — Project Bootstrap

- [ ] **0.1** Initialize Maven project, set Java 21 target
- [ ] **0.2** Add JavaFX 21 dependencies + Maven plugin for `javafx:run`
- [ ] **0.3** Add Jackson + JUnit 5
- [ ] **0.4** Create empty package structure matching Section 4
- [ ] **0.5** Minimal `App.java` launches an empty 1280×800 JavaFX window

### Phase 1 — Domain Foundations

- [ ] **1.1** `PixelBuffer` (width, height, raw `int[]` ARGB array, get/set pixel)
- [ ] **1.2** `ImageMetadata` record (name, format, fileSize, width, height)
- [ ] **1.3** `ColorSpace` enum + `ColorTriplet` record
- [ ] **1.4** Conversion math for all 6 spaces: RGB ↔ HSV, RGB ↔ CMYK, RGB ↔ YUV, RGB ↔ YCbCr, RGB ↔ LAB
- [ ] **1.5** Unit tests: known values + RGB→X→RGB round-trip tolerance

### Phase 2 — Image Workspace (Req 1, 8)

- [ ] **2.1** `ImageLoader` interface in domain; `FileSystemImageLoader` impl in infrastructure
- [ ] **2.2** `LoadImageUseCase` returning `PixelBuffer` + `ImageMetadata`
- [ ] **2.3** `ImageWorkspaceViewModel` exposing current image as observable
- [ ] **2.4** Main window 3-pane layout (left / center / right) shell
- [ ] **2.5** Image canvas component that renders a `PixelBuffer` to a `WritableImage`
- [ ] **2.6** Top toolbar: Open file picker → load → display
- [ ] **2.7** Drag-and-drop on canvas: drop file → load → display
- [ ] **2.8** Metadata panel binds to ViewModel (name, format, size, dimensions)

### Phase 3 — Recent Files (JSON Persistence)

- [ ] **3.1** `RecentFile` record (id, path, lastOpenedAt)
- [ ] **3.2** `RecentFilesStore` interface; `JsonRecentFilesStore` impl reading/writing `~/.pixellab/recent.json`
- [ ] **3.3** `LoadRecentFilesUseCase`, `AddRecentFileUseCase`, `RemoveRecentFileUseCase`
- [ ] **3.4** Left-panel list view bound to recent files observable
- [ ] **3.5** Click on item → load image into workspace
- [ ] **3.6** If file no longer exists: show popup, remove entry, refresh list

### Phase 4 — Edit Session (Req 9, 10)

- [ ] **4.1** `EditSession` holds `originalBuffer` (immutable reference) + `workingBuffer`
- [ ] **4.2** `ResetUseCase` — copy original pixels back into working buffer
- [ ] **4.3** `SaveImageUseCase` — encode working buffer to original path's format
- [ ] **4.4** `SaveAsImageUseCase` — user picks path + format
- [ ] **4.5** Toolbar buttons: Save, Save As, Reset (disabled when no image loaded)
- [ ] **4.6** Format selection dialog for Save As (PNG, JPG, BMP)

### Phase 5 — Color Space Conversion (Req 2)

- [ ] **5.1** `ConvertColorSpaceUseCase` — operates on a `PixelBuffer`, returns new buffer in target space
- [ ] **5.2** Color space selector in left panel (dropdown or radio group)
- [ ] **5.3** Wire selector → use case → working buffer update → canvas refresh
- [ ] **5.4** Visual sanity test: RGB → HSV → RGB displays correctly

### Phase 6 — Channel Manipulation (Req 3)

- [ ] **6.1** `SplitChannelsUseCase` — returns 3 grayscale buffers, one per channel
- [ ] **6.2** `ModifyChannelUseCase` — add offset / multiply / set value on one channel
- [ ] **6.3** `DisableChannelUseCase` — zero out a channel and reconstruct
- [ ] **6.4** Channel preview strip in right panel (3 thumbnails)
- [ ] **6.5** Per-channel slider + on/off toggle for each channel of current space
- [ ] **6.6** Live reconstruction preview as user adjusts

### Phase 7 — Color Quantization (Req 7)

- [ ] **7.1** `QuantizeColorsUseCase` (start with median-cut algorithm)
- [ ] **7.2** Slider 2–256 colors with current value label
- [ ] **7.3** Wire to canvas via real-time pipeline

### Phase 8 — 3D Visualization (Req 4, 5)

- [ ] **8.1** `ColorSpaceVisualizationViewModel`
- [ ] **8.2** 3D `SubScene` with `PerspectiveCamera` and lighting
- [ ] **8.3** RGB cube renderer (8 colored vertices, gradient faces)
- [ ] **8.4** HSV cylinder renderer
- [ ] **8.5** Renderers for LAB, YUV, YCbCr, CMYK (shape per space)
- [ ] **8.6** Mouse drag → rotate; scroll → zoom
- [ ] **8.7** Click in 3D space → pick color at that point
- [ ] **8.8** Picked color displayed in current space
- [ ] **8.9** Synchronization panel: same color shown in all 6 systems simultaneously

### Phase 9 — Real-Time Plumbing (Req 6) — *cross-cutting*

> Some of this lands while building Phases 5–8. Do a dedicated polish pass at the end.

- [ ] **9.1** `BackgroundExecutor` — single-thread, latest-wins task queue
- [ ] **9.2** `UpdateCoalescer` — drops intermediate slider events while one is processing
- [ ] **9.3** Refactor pixel-loop use cases to run via the executor
- [ ] **9.4** Stress-test on a 4000×3000 image — slider drag must stay smooth
- [ ] **9.5** Spinner / loading indicator for any operation > 200 ms

### Phase 10 — Polish & Deliverables

- [ ] **10.1** Error dialogs: corrupted image, save failure, unsupported format
- [ ] **10.2** Keyboard shortcuts: Ctrl+O, Ctrl+S, Ctrl+Shift+S, Ctrl+R
- [ ] **10.3** Empty-state UI when no image is loaded
- [ ] **10.4** App icon + window title
- [ ] **10.5** Build runnable fat-JAR (jlink or shade plugin)
- [ ] **10.6** Write report: procedures used to fulfill each requirement
- [ ] **10.7** README with screenshots and run instructions
- [ ] **10.8** End-to-end smoke test: open → convert → tweak channel → quantize → save

---

## 7. Requirements Coverage Map

| Req # | Description | Phase |
|---|---|---|
| 1 | Image input (GUI + drag-drop) | 2 |
| 2 | Convert between RGB/CMYK/HSV/YUV/LAB/YCbCr | 5 |
| 3 | Display channels separately + control them | 6 |
| 4 | 2D/3D color space representations | 8 |
| 5 | Rotate, zoom, pick, synchronize values | 8 |
| 6 | Real-time updates | 9 (cross-cutting) |
| 7 | Color quantization | 7 |
| 8 | Image metadata | 2 |
| 9 | Reset to original | 4 |
| 10 | Save / Save As | 4 |

---

## 8. Progress Snapshot

- **Phase 0:** ☐ Not started
- **Phase 1:** ☐ Not started
- **Phase 2:** ☐ Not started
- **Phase 3:** ☐ Not started
- **Phase 4:** ☐ Not started
- **Phase 5:** ☐ Not started
- **Phase 6:** ☐ Not started
- **Phase 7:** ☐ Not started
- **Phase 8:** ☐ Not started
- **Phase 9:** ☐ Not started
- **Phase 10:** ☐ Not started

> Update each phase to ☐ In progress / ☑ Done as you advance.