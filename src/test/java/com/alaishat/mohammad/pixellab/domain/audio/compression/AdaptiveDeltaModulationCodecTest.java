package com.alaishat.mohammad.pixellab.domain.audio.compression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdaptiveDeltaModulationCodecTest {

    private static final int BIT_DEPTH = AudioSamples.BIT_DEPTH;
    private static final int MAGNITUDE = 1 << (BIT_DEPTH - 1);

    private final AdaptiveDeltaModulationCodec codec = new AdaptiveDeltaModulationCodec();

    /**
     * Hand-traced with stepSize = 10, adaptationFactor = 2, predicted starting
     * at 0. Every sample (100) is at or above the predictor, so every emitted
     * bit is 1 — consecutive equal bits double the step each time:
     * <pre>
     *   bit 1 (first bit, no previous): step 10 -> 10/2  =  5, predicted ->  5
     *   bit 1 (repeat):                 step  5 ->  5*2  = 10, predicted -> 15
     *   bit 1 (repeat):                 step 10 -> 10*2  = 20, predicted -> 35
     *   bit 1 (repeat):                 step 20 -> 20*2  = 40, predicted -> 75
     * </pre>
     * and decoding replays exactly the same adaptation walk.
     */
    @Test
    void knownValues_constantSignalDoublesStepEachSample() {
        CompressionSettings settings = new CompressionSettings(4, 10.0, 2.0);
        int[] samples = AudioSamples.constant(4, 100);

        byte[] encoded = codec.encode(samples, BIT_DEPTH, settings, CompressionProgressListener.NONE);
        int[] decoded = codec.decode(encoded, samples.length, BIT_DEPTH, settings, CompressionProgressListener.NONE);

        assertArrayEquals(new int[] {5, 15, 35, 75}, decoded);
    }

    @Test
    void roundTrip_isDeterministicAndStaysInPcmRange() {
        CompressionSettings settings = new CompressionSettings(4, 256.0, 1.5);

        for (int[] samples : AudioSamples.REPRESENTATIVE) {
            byte[] encodedOnce = codec.encode(samples, BIT_DEPTH, settings, CompressionProgressListener.NONE);
            byte[] encodedAgain = codec.encode(samples, BIT_DEPTH, settings, CompressionProgressListener.NONE);
            assertArrayEquals(encodedOnce, encodedAgain, "encode must be deterministic for the same input");

            int[] decodedOnce = codec.decode(encodedOnce, samples.length, BIT_DEPTH, settings, CompressionProgressListener.NONE);
            int[] decodedAgain = codec.decode(encodedOnce, samples.length, BIT_DEPTH, settings, CompressionProgressListener.NONE);
            assertArrayEquals(decodedOnce, decodedAgain, "decode must be deterministic for the same input");

            assertEquals(samples.length, decodedOnce.length);
            for (int value : decodedOnce) {
                assertTrue(value >= -MAGNITUDE && value < MAGNITUDE,
                        "decoded sample " + value + " outside valid " + BIT_DEPTH + "-bit PCM range");
            }
        }
    }
}
