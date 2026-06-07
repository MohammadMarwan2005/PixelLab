package com.alaishat.mohammad.pixellab.domain.audio.compression;

/**
 * User-tunable compression parameters (Req. 6). Not every field is meaningful
 * to every algorithm:
 *
 * <ul>
 *   <li>{@code quantizationBits} — bits per encoded residual; used by
 *       {@link CompressionAlgorithm#DPCM} (1-bit codecs ignore it).</li>
 *   <li>{@code stepSize} — the (initial) delta step, in raw PCM sample units;
 *       used by {@link CompressionAlgorithm#DELTA_MODULATION} and
 *       {@link CompressionAlgorithm#ADAPTIVE_DELTA_MODULATION}.</li>
 *   <li>{@code adaptationFactor} — multiplier the adaptive codec grows/shrinks
 *       its step by; used only by {@link CompressionAlgorithm#ADAPTIVE_DELTA_MODULATION}.</li>
 * </ul>
 *
 * Keeping one flat record (rather than a sealed hierarchy of per-algorithm
 * settings) keeps the settings panel and the use case simple — exactly the
 * "simplify it" brief — at the cost of a few unused fields per algorithm.
 */
public record CompressionSettings(int quantizationBits, double stepSize, double adaptationFactor) {

    public static final int MIN_QUANTIZATION_BITS = 2;
    public static final int MAX_QUANTIZATION_BITS = 8;

    public CompressionSettings {
        if (quantizationBits < MIN_QUANTIZATION_BITS || quantizationBits > MAX_QUANTIZATION_BITS) {
            throw new IllegalArgumentException(
                    "quantizationBits must be in [" + MIN_QUANTIZATION_BITS + ", " + MAX_QUANTIZATION_BITS + "]: "
                            + quantizationBits);
        }
        if (stepSize <= 0) {
            throw new IllegalArgumentException("stepSize must be positive: " + stepSize);
        }
        if (adaptationFactor <= 1.0) {
            throw new IllegalArgumentException("adaptationFactor must be > 1.0: " + adaptationFactor);
        }
    }

    public static CompressionSettings defaults() {
        return new CompressionSettings(4, 256.0, 1.5);
    }
}
