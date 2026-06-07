package com.alaishat.mohammad.pixellab.domain.audio.compression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeltaModulationCodecTest {

    private static final int BIT_DEPTH = AudioSamples.BIT_DEPTH;
    private static final int MAGNITUDE = 1 << (BIT_DEPTH - 1);

    private final DeltaModulationCodec codec = new DeltaModulationCodec();

    /**
     * Hand-traced: predicted starts at 0. Every sample is silence (0), and the
     * comparison is "{@code >=}", so the predictor walks +step, -step, +step, …
     * forever, oscillating around zero and reproducing exactly that pattern.
     */
    @Test
    void knownValues_silenceProducesAlternatingStepPattern() {
        CompressionSettings settings = new CompressionSettings(4, 10.0, 1.5);
        int[] samples = AudioSamples.silence(8);

        byte[] encoded = codec.encode(samples, BIT_DEPTH, settings, CompressionProgressListener.NONE);
        int[] decoded = codec.decode(encoded, samples.length, BIT_DEPTH, settings, CompressionProgressListener.NONE);

        assertArrayEquals(new int[] {10, 0, 10, 0, 10, 0, 10, 0}, decoded);
    }

    /**
     * Hand-traced with step = 10, predicted starting at 0:
     * <pre>
     *   sample  50 >= 0   -> bit 1, predicted -> 10
     *   sample  50 >= 10  -> bit 1, predicted -> 20
     *   sample   5 >= 20? -> bit 0, predicted -> 10
     *   sample   5 >= 10? -> bit 0, predicted ->  0
     * </pre>
     * and decoding replays exactly the same predictor walk.
     */
    @Test
    void knownValues_handTracedSequence() {
        CompressionSettings settings = new CompressionSettings(4, 10.0, 1.5);
        int[] samples = {50, 50, 5, 5};

        byte[] encoded = codec.encode(samples, BIT_DEPTH, settings, CompressionProgressListener.NONE);
        int[] decoded = codec.decode(encoded, samples.length, BIT_DEPTH, settings, CompressionProgressListener.NONE);

        assertArrayEquals(new int[] {10, 20, 10, 0}, decoded);
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
