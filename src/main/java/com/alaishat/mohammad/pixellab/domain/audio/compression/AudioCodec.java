package com.alaishat.mohammad.pixellab.domain.audio.compression;

/**
 * One waveform-coding algorithm. Every implementation has the same shape —
 * {@code encode}/{@code decode} on a single channel's raw PCM samples — mirroring
 * how {@code domain.color.conversion} defines stateless {@code toXxx}/{@code toRgb}
 * pairs per color space. Unlike those, these are necessarily *stateful within a
 * call* (each sample's code depends on the running predictor/step), so the
 * "stateless utility" shape becomes "one self-contained loop per direction".
 *
 * <p>Implementations must report progress and honor cancellation via the
 * supplied {@link CompressionProgressListener} (Reqs. 7 and 8) by calling it
 * periodically — every few thousand samples, not every sample.
 */
public interface AudioCodec {

    CompressionAlgorithm algorithm();

    /**
     * @param samples   raw signed PCM samples for one channel
     * @param bitDepth  bits per sample, defines the valid output range
     * @param settings  user-chosen compression parameters
     * @param listener  progress/cancellation callback
     * @return the packed, encoded bitstream for this channel
     */
    byte[] encode(int[] samples, int bitDepth, CompressionSettings settings, CompressionProgressListener listener);

    /**
     * @param encoded    packed bitstream produced by {@link #encode}
     * @param frameCount number of samples to reconstruct
     * @param bitDepth   bits per sample, defines the valid output range
     * @param settings   the exact settings used to encode — required to decode correctly
     * @param listener   progress/cancellation callback
     * @return reconstructed signed PCM samples for one channel
     */
    int[] decode(byte[] encoded, int frameCount, int bitDepth, CompressionSettings settings,
                 CompressionProgressListener listener);
}
