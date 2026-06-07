package com.alaishat.mohammad.pixellab.domain.audio.compression;

/**
 * Summary shown after a compression run completes (Req. 10).
 *
 * @param originalBytes     size of the source PCM data, in bytes
 * @param compressedBytes   size of the encoded bitstream, in bytes (sum across channels)
 * @param elapsedMillis     wall-clock time the encode took
 * @param algorithm         the algorithm that was run
 * @param settings          the exact settings it was run with
 */
public record CompressionReport(
        long originalBytes,
        long compressedBytes,
        long elapsedMillis,
        CompressionAlgorithm algorithm,
        CompressionSettings settings
) {

    /** Fraction of the original size that was saved, as a percentage in [0, 100]. */
    public double savingsPercent() {
        if (originalBytes == 0) return 0;
        return 100.0 * (1.0 - (compressedBytes / (double) originalBytes));
    }

    /** originalBytes : compressedBytes, e.g. 4.0 means "4 times smaller". */
    public double compressionRatio() {
        if (compressedBytes == 0) return 0;
        return originalBytes / (double) compressedBytes;
    }
}
