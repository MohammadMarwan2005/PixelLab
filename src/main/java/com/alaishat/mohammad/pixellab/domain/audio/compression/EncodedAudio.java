package com.alaishat.mohammad.pixellab.domain.audio.compression;

import java.util.Objects;

/**
 * The result of compressing an {@link com.alaishat.mohammad.pixellab.domain.audio.AudioBuffer}:
 * one packed bitstream per channel plus everything {@link AudioCodec#decode}
 * needs to reconstruct it, and everything the on-disk container (Req. 11)
 * needs to round-trip.
 *
 * <p>{@code totalEncodedBytes()} is the size used for the compression-ratio
 * report (Req. 10); it intentionally excludes the small fixed header.
 */
public record EncodedAudio(
        CompressionAlgorithm algorithm,
        CompressionSettings settings,
        int sampleRate,
        int bitDepth,
        int frameCount,
        byte[][] channels
) {

    public EncodedAudio {
        Objects.requireNonNull(algorithm, "algorithm");
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(channels, "channels");
        if (channels.length == 0) {
            throw new IllegalArgumentException("must have at least one channel");
        }
    }

    public int channelCount() {
        return channels.length;
    }

    public long totalEncodedBytes() {
        long total = 0;
        for (byte[] channel : channels) {
            total += channel.length;
        }
        return total;
    }
}
