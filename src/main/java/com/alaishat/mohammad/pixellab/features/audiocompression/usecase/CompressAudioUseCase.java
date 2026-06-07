package com.alaishat.mohammad.pixellab.features.audiocompression.usecase;

import com.alaishat.mohammad.pixellab.domain.audio.AudioBuffer;
import com.alaishat.mohammad.pixellab.domain.audio.compression.AudioCodec;
import com.alaishat.mohammad.pixellab.domain.audio.compression.CompressionAlgorithm;
import com.alaishat.mohammad.pixellab.domain.audio.compression.CompressionProgressListener;
import com.alaishat.mohammad.pixellab.domain.audio.compression.CompressionSettings;
import com.alaishat.mohammad.pixellab.domain.audio.compression.EncodedAudio;

import java.util.Map;
import java.util.Objects;

/**
 * Runs the chosen {@link AudioCodec} over every channel of the working buffer
 * and packages the result as an {@link EncodedAudio} (Req. 4). Pure domain
 * orchestration — {@code AudioCompressionViewModel} drives this from a
 * {@code javafx.concurrent.Task} so it can report progress and be cancelled.
 */
public final class CompressAudioUseCase {

    private final Map<CompressionAlgorithm, AudioCodec> codecs;

    public CompressAudioUseCase(Map<CompressionAlgorithm, AudioCodec> codecs) {
        this.codecs = Objects.requireNonNull(codecs, "codecs");
    }

    public EncodedAudio execute(AudioBuffer buffer, CompressionAlgorithm algorithm,
                                CompressionSettings settings, CompressionProgressListener listener) {
        Objects.requireNonNull(buffer, "buffer");
        Objects.requireNonNull(algorithm, "algorithm");
        Objects.requireNonNull(settings, "settings");

        AudioCodec codec = codecs.get(algorithm);
        if (codec == null) {
            throw new IllegalArgumentException("No codec registered for " + algorithm);
        }

        int channelCount = buffer.channelCount();
        int frameCount = buffer.frameCount();
        byte[][] channels = new byte[channelCount][];
        for (int c = 0; c < channelCount; c++) {
            CompressionProgressListener channelListener = MultiChannelProgress.scoped(listener, c, channelCount, frameCount);
            channels[c] = codec.encode(buffer.data()[c], buffer.bitDepth(), settings, channelListener);
        }

        return new EncodedAudio(algorithm, settings, buffer.sampleRate(), buffer.bitDepth(), frameCount, channels);
    }
}
