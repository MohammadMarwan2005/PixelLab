package com.alaishat.mohammad.pixellab.domain.audio.compression;

/**
 * Differential Pulse Code Modulation with a first-order predictor (the
 * previously *reconstructed* sample) and a uniform quantizer over the
 * residual range:
 *
 * <pre>
 *   residual  = sample[n] - predicted
 *   index     = quantize(residual) into 2^quantizationBits levels spanning [-range, range)
 *   predicted = predicted + dequantize(index)        // both sides reconstruct identically
 * </pre>
 *
 * Using the *reconstructed* (not original) previous sample as the predictor —
 * and quantizing to a fixed level count rather than a fixed step — is what
 * lets the decoder reproduce the exact same predictor sequence the encoder saw,
 * without needing to transmit anything beyond the quantized indices.
 */
public final class DpcmCodec implements AudioCodec {

    @Override
    public CompressionAlgorithm algorithm() {
        return CompressionAlgorithm.DPCM;
    }

    @Override
    public byte[] encode(int[] samples, int bitDepth, CompressionSettings settings, CompressionProgressListener listener) {
        Quantizer quantizer = new Quantizer(settings.quantizationBits(), bitDepth);
        BitWriter writer = new BitWriter();
        int predicted = 0;

        for (int i = 0; i < samples.length; i++) {
            int residual = samples[i] - predicted;
            int index = quantizer.quantize(residual);
            writer.writeBits(index, quantizer.bitsPerCode);
            predicted = PcmRange.clamp(predicted + quantizer.dequantize(index), bitDepth);
            ProgressReporting.tick(listener, i, samples.length);
        }
        return writer.toByteArray();
    }

    @Override
    public int[] decode(byte[] encoded, int frameCount, int bitDepth, CompressionSettings settings,
                        CompressionProgressListener listener) {
        Quantizer quantizer = new Quantizer(settings.quantizationBits(), bitDepth);
        BitReader reader = new BitReader(encoded);
        int[] out = new int[frameCount];
        int predicted = 0;

        for (int i = 0; i < frameCount; i++) {
            int index = reader.readBits(quantizer.bitsPerCode);
            predicted = PcmRange.clamp(predicted + quantizer.dequantize(index), bitDepth);
            out[i] = predicted;
            ProgressReporting.tick(listener, i, frameCount);
        }
        return out;
    }

    /**
     * Uniform quantizer over the residual range {@code [-range, range)}, where
     * {@code range} is the full PCM magnitude (a residual between two samples
     * can span the whole signal). Maps a residual to one of {@code 2^bits}
     * evenly spaced levels and back to that level's midpoint.
     */
    private static final class Quantizer {
        final int bitsPerCode;
        final int levels;
        final double step;
        final int range;

        Quantizer(int bitsPerCode, int bitDepth) {
            this.bitsPerCode = bitsPerCode;
            this.levels = 1 << bitsPerCode;
            this.range = PcmRange.magnitude(bitDepth);
            this.step = (2.0 * range) / levels;
        }

        int quantize(int residual) {
            int index = (int) Math.floor((residual + range) / step);
            return Math.max(0, Math.min(levels - 1, index));
        }

        int dequantize(int index) {
            return (int) Math.round((index + 0.5) * step - range);
        }
    }
}
