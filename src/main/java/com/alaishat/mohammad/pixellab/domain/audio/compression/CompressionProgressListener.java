package com.alaishat.mohammad.pixellab.domain.audio.compression;

/**
 * Callback a codec reports progress to and checks for cancellation through
 * (Req. 7 and 8). Codecs call {@link #onProgress} every few thousand samples —
 * not every sample, which would dominate the running time — and throw
 * {@link CompressionCancelledException} via {@link #checkCancelled()} so the
 * encode/decode loop can unwind cleanly.
 */
public interface CompressionProgressListener {

    CompressionProgressListener NONE = new CompressionProgressListener() {
        @Override public void onProgress(long processed, long total) {}
        @Override public boolean isCancelled() { return false; }
    };

    /** Reports that {@code processed} of {@code total} samples have been handled. */
    void onProgress(long processed, long total);

    boolean isCancelled();

    default void checkCancelled() {
        if (isCancelled()) {
            throw new CompressionCancelledException();
        }
    }
}
