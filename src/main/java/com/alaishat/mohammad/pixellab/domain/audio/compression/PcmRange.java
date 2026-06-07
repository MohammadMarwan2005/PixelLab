package com.alaishat.mohammad.pixellab.domain.audio.compression;

/**
 * Signed-PCM range helpers shared by every codec — keeps the predictor/integrator
 * math (which must clamp identically on encode and decode to stay in sync) in
 * one place rather than copied three times.
 */
final class PcmRange {

    private PcmRange() {}

    /** Magnitude of the signed range for {@code bitDepth} bits, e.g. 32768 for 16-bit. */
    static int magnitude(int bitDepth) {
        return 1 << (bitDepth - 1);
    }

    static int clamp(int value, int bitDepth) {
        int magnitude = magnitude(bitDepth);
        return Math.max(-magnitude, Math.min(magnitude - 1, value));
    }

    static double clamp(double value, int bitDepth) {
        int magnitude = magnitude(bitDepth);
        return Math.max(-magnitude, Math.min(magnitude - 1, value));
    }
}
