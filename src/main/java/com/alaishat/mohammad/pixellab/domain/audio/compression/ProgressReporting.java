package com.alaishat.mohammad.pixellab.domain.audio.compression;

/**
 * Shared progress/cancellation cadence for codec loops — reporting on every
 * sample would dominate the running time, so codecs check in only periodically.
 */
final class ProgressReporting {

    private static final long INTERVAL = 4096;

    private ProgressReporting() {}

    /** Call once per sample with its zero-based {@code index}; throttles internally. */
    static void tick(CompressionProgressListener listener, long index, long total) {
        if (index % INTERVAL == 0 || index == total - 1) {
            listener.onProgress(index + 1, total);
            listener.checkCancelled();
        }
    }
}
