package com.alaishat.mohammad.pixellab.domain.audio.compression;

/**
 * The three waveform-coding algorithms PixelLab supports (Req. 4). All three
 * share one shape — predict the next sample, encode the residual against that
 * prediction in fewer bits, reconstruct by accumulating residuals — escalating
 * in sophistication from a fixed-step 1-bit code to an adaptive-step 1-bit code
 * to a multi-bit quantized difference.
 */
public enum CompressionAlgorithm {
    DELTA_MODULATION("Delta Modulation"),
    ADAPTIVE_DELTA_MODULATION("Adaptive Delta Modulation"),
    DPCM("Differential Pulse Code Modulation (DPCM)");

    private final String displayName;

    CompressionAlgorithm(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
