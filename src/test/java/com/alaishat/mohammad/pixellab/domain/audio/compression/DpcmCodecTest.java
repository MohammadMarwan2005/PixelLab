package com.alaishat.mohammad.pixellab.domain.audio.compression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DpcmCodecTest {

    private static final int BIT_DEPTH = AudioSamples.BIT_DEPTH;
    private static final int MAGNITUDE = 1 << (BIT_DEPTH - 1);

    private final DpcmCodec codec = new DpcmCodec();

    /**
     * Hand-traced with quantizationBits = 2 (4 levels, step = 2*32768/4 =
     * 16384), predicted starting at 0. Each step quantizes the residual against
     * the *previous reconstructed* value:
     * <pre>
     *   residual 1000 -0     = 1000  -> index 2 -> +8192  -> predicted  8192
     *   residual 2000 -8192  = -6192 -> index 1 -> -8192  -> predicted     0
     *   residual -500 -0     = -500  -> index 1 -> -8192  -> predicted -8192
     *   residual  100 -(-8192)= 8292 -> index 2 -> +8192  -> predicted     0
     * </pre>
     */
    @Test
    void knownValues_handTracedSequence() {
        CompressionSettings settings = new CompressionSettings(2, 256.0, 1.5);
        int[] samples = {1000, 2000, -500, 100};

        byte[] encoded = codec.encode(samples, BIT_DEPTH, settings, CompressionProgressListener.NONE);
        int[] decoded = codec.decode(encoded, samples.length, BIT_DEPTH, settings, CompressionProgressListener.NONE);

        assertArrayEquals(new int[] {8192, 0, -8192, 0}, decoded);
    }

    /**
     * Structural guarantee of DPCM with a uniform quantizer and a "previous
     * reconstructed sample" predictor: the decoder reproduces the *exact*
     * predictor sequence the encoder computed (it replays the same residual ->
     * index -> dequantized-residual chain), so the only error between the
     * original and the round-tripped signal is the per-sample quantization
     * error of the residual — which a uniform quantizer bounds to
     * ±half a quantization step. Crucially, this error does not accumulate.
     */
    @Test
    void roundTrip_errorIsBoundedByHalfTheQuantizationStep() {
        for (int quantizationBits : new int[] {2, 4, 8}) {
            CompressionSettings settings = new CompressionSettings(quantizationBits, 256.0, 1.5);
            double step = (2.0 * MAGNITUDE) / (1 << quantizationBits);
            double tolerance = step / 2.0 + 1.0; // + rounding slack

            for (int[] samples : AudioSamples.REPRESENTATIVE) {
                byte[] encoded = codec.encode(samples, BIT_DEPTH, settings, CompressionProgressListener.NONE);
                int[] decoded = codec.decode(encoded, samples.length, BIT_DEPTH, settings, CompressionProgressListener.NONE);

                assertEqualsLength(samples, decoded);
                for (int i = 0; i < samples.length; i++) {
                    assertTrue(Math.abs(samples[i] - decoded[i]) <= tolerance,
                            "sample " + i + ": |" + samples[i] + " - " + decoded[i] + "| exceeds " + tolerance
                                    + " for quantizationBits=" + quantizationBits);
                }
            }
        }
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

            for (int value : decodedOnce) {
                assertTrue(value >= -MAGNITUDE && value < MAGNITUDE,
                        "decoded sample " + value + " outside valid " + BIT_DEPTH + "-bit PCM range");
            }
        }
    }

    private static void assertEqualsLength(int[] samples, int[] decoded) {
        assertTrue(samples.length == decoded.length,
                "expected " + samples.length + " decoded frames, got " + decoded.length);
    }
}
