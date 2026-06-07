package com.alaishat.mohammad.pixellab.domain.audio;

import java.nio.file.Path;
import java.util.Objects;

/**
 * One audio edit, scoped to a loaded file. Mirrors
 * {@link com.alaishat.mohammad.pixellab.domain.image.EditSession}:
 *
 *  - {@code originalBuffer}: immutable reference, never written to. Source for
 *    Reset (Req. 9) and for computing compression ratios against.
 *  - {@code workingBuffer}: mutable, what playback/properties reflect. Replaced
 *    wholesale via {@link #replaceWorking(AudioBuffer)} — e.g. after a
 *    compress→decompress round trip installs the reconstructed audio so it can
 *    be previewed, compared, and saved.
 *  - {@code sourcePath} + {@code originalFormat}: needed by Save to round-trip
 *    through the same on-disk container the file came in as.
 */
public final class AudioEditSession {

    private final AudioBuffer originalBuffer;
    private final Path sourcePath;
    private final String originalFormat;
    private AudioBuffer workingBuffer;

    public AudioEditSession(AudioBuffer original, Path sourcePath, String originalFormat) {
        this.originalBuffer = Objects.requireNonNull(original, "original");
        this.sourcePath = Objects.requireNonNull(sourcePath, "sourcePath");
        this.originalFormat = Objects.requireNonNull(originalFormat, "originalFormat");
        this.workingBuffer = original.copy();
    }

    public AudioBuffer originalBuffer() {
        return originalBuffer;
    }

    public AudioBuffer workingBuffer() {
        return workingBuffer;
    }

    public Path sourcePath() {
        return sourcePath;
    }

    public String originalFormat() {
        return originalFormat;
    }

    public void replaceWorking(AudioBuffer newWorking) {
        this.workingBuffer = Objects.requireNonNull(newWorking, "newWorking");
    }

    public void resetToOriginal() {
        this.workingBuffer = originalBuffer.copy();
    }
}
