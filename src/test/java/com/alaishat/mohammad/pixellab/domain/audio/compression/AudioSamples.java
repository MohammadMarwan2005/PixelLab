package com.alaishat.mohammad.pixellab.domain.audio.compression;

import java.util.List;

/**
 * Representative 16-bit PCM sample arrays shared by the codec round-trip
 * tests. Covers silence, a constant offset, and slowly/quickly varying sine
 * waves — enough to exercise both the "predictor keeps up" and "predictor
 * lags behind a fast transition" branches of the differential codecs.
 * Mirrors {@code domain.color.conversion.ColorSamples}.
 */
final class AudioSamples {

    private AudioSamples() {}

    static final int BIT_DEPTH = 16;

    static int[] silence(int length) {
        return new int[length];
    }

    static int[] constant(int length, int value) {
        int[] samples = new int[length];
        java.util.Arrays.fill(samples, value);
        return samples;
    }

    /** A sine wave of the given peak amplitude completing {@code cycles} full periods over {@code length} frames. */
    static int[] sineWave(int length, int amplitude, double cycles) {
        int[] samples = new int[length];
        for (int i = 0; i < length; i++) {
            samples[i] = (int) Math.round(amplitude * Math.sin(2 * Math.PI * cycles * i / length));
        }
        return samples;
    }

    static final List<int[]> REPRESENTATIVE = List.of(
            silence(256),
            constant(256, 5000),
            constant(256, -12000),
            sineWave(512, 12000, 4.0),
            sineWave(2000, 30000, 17.0)
    );
}
