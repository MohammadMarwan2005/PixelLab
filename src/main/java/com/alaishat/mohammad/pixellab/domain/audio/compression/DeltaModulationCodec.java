package com.alaishat.mohammad.pixellab.domain.audio.compression;

/**
 * Classic 1-bit Delta Modulation: track a running predictor and, for every
 * sample, emit a single bit recording whether the actual sample is at or above
 * it — then nudge the predictor by a fixed {@code stepSize} in that direction.
 * Decoding simply replays the same nudges.
 *
 * <pre>
 *   if sample[n] >= predicted: bit = 1, predicted += step
 *   else:                      bit = 0, predicted -= step
 * </pre>
 *
 * The predictor is clamped to the valid PCM range on both sides so encode and
 * decode can never drift out of sync.
 */
public final class DeltaModulationCodec implements AudioCodec {

    @Override
    public CompressionAlgorithm algorithm() {
        return CompressionAlgorithm.DELTA_MODULATION;
    }

    @Override
    public byte[] encode(int[] samples, int bitDepth, CompressionSettings settings, CompressionProgressListener listener) {
        double step = settings.stepSize();
        double predicted = 0;
        BitWriter writer = new BitWriter();

        for (int i = 0; i < samples.length; i++) {
            int bit = (samples[i] >= predicted) ? 1 : 0;
            writer.writeBit(bit);
            predicted = PcmRange.clamp(predicted + (bit == 1 ? step : -step), bitDepth);
            ProgressReporting.tick(listener, i, samples.length);
        }
        return writer.toByteArray();
    }

    @Override
    public int[] decode(byte[] encoded, int frameCount, int bitDepth, CompressionSettings settings,
                        CompressionProgressListener listener) {
        double step = settings.stepSize();
        double predicted = 0;
        BitReader reader = new BitReader(encoded);
        int[] out = new int[frameCount];

        for (int i = 0; i < frameCount; i++) {
            int bit = reader.readBit();
            predicted = PcmRange.clamp(predicted + (bit == 1 ? step : -step), bitDepth);
            out[i] = PcmRange.clamp((int) Math.round(predicted), bitDepth);
            ProgressReporting.tick(listener, i, frameCount);
        }
        return out;
    }

}
