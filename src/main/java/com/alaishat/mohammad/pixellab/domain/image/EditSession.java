package com.alaishat.mohammad.pixellab.domain.image;

import java.nio.file.Path;
import java.util.Objects;

/**
 * One image edit, scoped to a loaded file. Holds:
 *
 *  - {@code originalBuffer}: immutable reference, never written to. Used as the
 *    source for Reset (Req 9) and to compare against the working state.
 *  - {@code workingBuffer}: mutable, what the canvas displays and use cases mutate.
 *    Replaceable via {@link #replaceWorking(PixelBuffer)} so transformations like
 *    color-space conversion (Phase 5) can install a fresh buffer of the same dims.
 *  - {@code sourcePath} + {@code originalFormat}: needed by Save to round-trip
 *    through the same on-disk format the file came in as.
 */
public final class EditSession {

    private final PixelBuffer originalBuffer;
    private final Path sourcePath;
    private final String originalFormat;
    private PixelBuffer workingBuffer;

    public EditSession(PixelBuffer original, Path sourcePath, String originalFormat) {
        this.originalBuffer = Objects.requireNonNull(original, "original");
        this.sourcePath = Objects.requireNonNull(sourcePath, "sourcePath");
        this.originalFormat = Objects.requireNonNull(originalFormat, "originalFormat");
        this.workingBuffer = original.copy();
    }

    public PixelBuffer originalBuffer() {
        return originalBuffer;
    }

    public PixelBuffer workingBuffer() {
        return workingBuffer;
    }

    public Path sourcePath() {
        return sourcePath;
    }

    public String originalFormat() {
        return originalFormat;
    }

    public void replaceWorking(PixelBuffer newWorking) {
        this.workingBuffer = Objects.requireNonNull(newWorking, "newWorking");
    }
}
