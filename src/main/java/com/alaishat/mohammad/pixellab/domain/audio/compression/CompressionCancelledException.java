package com.alaishat.mohammad.pixellab.domain.audio.compression;

/**
 * Thrown by a codec's encode/decode loop when {@link CompressionProgressListener#isCancelled()}
 * reports true (Req. 8). The use case catches this to unwind to a clean
 * "cancelled" outcome rather than treating it as a failure.
 */
public final class CompressionCancelledException extends RuntimeException {
    public CompressionCancelledException() {
        super("Compression cancelled");
    }
}
