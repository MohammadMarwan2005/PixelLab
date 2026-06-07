package com.alaishat.mohammad.pixellab.domain.audio.compression;

/**
 * Adaptive Delta Modulation: the same 1-bit-per-sample scheme as
 * {@link DeltaModulationCodec}, except the step size grows when consecutive
 * bits agree (the signal is changing quickly in one direction — track it
 * faster) and shrinks when they disagree (the signal is near a turning point —
 * slow down to avoid overshoot/granular noise). This is the textbook
 * "continuously variable slope" adaptation rule.
 *
 * <pre>
 *   bit = (sample[n] >= predicted) ? 1 : 0
 *   step = (bit == previousBit) ? step * factor : step / factor   // clamped to [step0/10, step0*10]
 *   predicted += (bit == 1) ? step : -step
 * </pre>
 *
 * Decoding replays the identical bit-driven adaptation, so encoder and decoder
 * step sequences never diverge.
 */
public final class AdaptiveDeltaModulationCodec implements AudioCodec {

    private static final double MIN_STEP_FACTOR = 0.1;
    private static final double MAX_STEP_FACTOR = 10.0;

    @Override
    public CompressionAlgorithm algorithm() {
        return CompressionAlgorithm.ADAPTIVE_DELTA_MODULATION;
    }

    @Override
    public byte[] encode(int[] samples, int bitDepth, CompressionSettings settings, CompressionProgressListener listener) {
        Adapter adapter = new Adapter(settings);
        BitWriter writer = new BitWriter();

        for (int i = 0; i < samples.length; i++) {
            int bit = (samples[i] >= adapter.predicted) ? 1 : 0;
            writer.writeBit(bit);
            adapter.advance(bit, bitDepth);
            ProgressReporting.tick(listener, i, samples.length);
        }
        return writer.toByteArray();
    }

    @Override
    public int[] decode(byte[] encoded, int frameCount, int bitDepth, CompressionSettings settings,
                        CompressionProgressListener listener) {
        Adapter adapter = new Adapter(settings);
        BitReader reader = new BitReader(encoded);
        int[] out = new int[frameCount];

        for (int i = 0; i < frameCount; i++) {
            int bit = reader.readBit();
            adapter.advance(bit, bitDepth);
            out[i] = PcmRange.clamp((int) Math.round(adapter.predicted), bitDepth);
            ProgressReporting.tick(listener, i, frameCount);
        }
        return out;
    }

    /** Holds the running predictor + step and the shared adaptation rule. */
    private static final class Adapter {
        private final double factor;
        private final double minStep;
        private final double maxStep;

        private double step;
        private double predicted;
        private int previousBit = -1;

        Adapter(CompressionSettings settings) {
            this.factor = settings.adaptationFactor();
            this.step = settings.stepSize();
            this.minStep = settings.stepSize() * MIN_STEP_FACTOR;
            this.maxStep = settings.stepSize() * MAX_STEP_FACTOR;
        }

        void advance(int bit, int bitDepth) {
            step = (bit == previousBit)
                    ? Math.min(maxStep, step * factor)
                    : Math.max(minStep, step / factor);
            predicted = PcmRange.clamp(predicted + (bit == 1 ? step : -step), bitDepth);
            previousBit = bit;
        }
    }
}
