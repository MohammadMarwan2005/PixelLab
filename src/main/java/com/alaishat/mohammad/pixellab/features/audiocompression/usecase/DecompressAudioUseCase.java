package com.alaishat.mohammad.pixellab.features.audiocompression.usecase;

import com.alaishat.mohammad.pixellab.domain.audio.AudioBuffer;
import com.alaishat.mohammad.pixellab.domain.audio.compression.AudioCodec;
import com.alaishat.mohammad.pixellab.domain.audio.compression.CompressionAlgorithm;
import com.alaishat.mohammad.pixellab.domain.audio.compression.CompressionProgressListener;
import com.alaishat.mohammad.pixellab.domain.audio.compression.EncodedAudio;

import java.util.Map;
import java.util.Objects;

/**
 * Reconstructs an {@link AudioBuffer} from an {@link EncodedAudio} by running
 * the matching {@link AudioCodec#decode} over every channel (Req. 5). The
 * result is installed as the workspace's working buffer so it can be played,
 * compared against the original, and saved as WAV.
 */
public final class DecompressAudioUseCase {

    private final Map<CompressionAlgorithm, AudioCodec> codecs;

    public DecompressAudioUseCase(Map<CompressionAlgorithm, AudioCodec> codecs) {
        this.codecs = Objects.requireNonNull(codecs, "codecs");
    }

    public AudioBuffer execute(EncodedAudio encoded, CompressionProgressListener listener) {
        Objects.requireNonNull(encoded, "encoded");

        AudioCodec codec = codecs.get(encoded.algorithm());
        if (codec == null) {
            throw new IllegalArgumentException("No codec registered for " + encoded.algorithm());
        }

        int channelCount = encoded.channelCount();
        int frameCount = encoded.frameCount();
        int[][] channels = new int[channelCount][];
        for (int c = 0; c < channelCount; c++) {
            CompressionProgressListener channelListener = MultiChannelProgress.scoped(listener, c, channelCount, frameCount);
            channels[c] = codec.decode(encoded.channels()[c], frameCount, encoded.bitDepth(), encoded.settings(), channelListener);
        }

        return new AudioBuffer(channels, encoded.sampleRate(), encoded.bitDepth());
    }
}
