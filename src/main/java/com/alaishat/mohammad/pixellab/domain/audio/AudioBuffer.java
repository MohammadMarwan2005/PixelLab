package com.alaishat.mohammad.pixellab.domain.audio;

import java.util.Arrays;
import java.util.Objects;

/**
 * Mutable container for decoded PCM audio: one signed sample array per channel,
 * all the same length (frame count), plus the format needed to interpret them.
 *
 * Mirrors {@link com.alaishat.mohammad.pixellab.domain.image.PixelBuffer}: the
 * backing arrays are exposed directly via {@link #data()} because compression
 * algorithms walk every sample and cannot afford per-access copies.
 */
public final class AudioBuffer {

    private final int[][] channels;
    private final int sampleRate;
    private final int bitDepth;

    public AudioBuffer(int[][] channels, int sampleRate, int bitDepth) {
        this.channels = Objects.requireNonNull(channels, "channels");
        if (channels.length == 0) {
            throw new IllegalArgumentException("must have at least one channel");
        }
        int frames = channels[0].length;
        for (int[] channel : channels) {
            if (channel.length != frames) {
                throw new IllegalArgumentException("all channels must have the same frame count");
            }
        }
        if (sampleRate <= 0) {
            throw new IllegalArgumentException("sampleRate must be positive: " + sampleRate);
        }
        if (bitDepth <= 0 || bitDepth > 32) {
            throw new IllegalArgumentException("bitDepth must be in (0, 32]: " + bitDepth);
        }
        this.sampleRate = sampleRate;
        this.bitDepth = bitDepth;
    }

    public int channelCount() {
        return channels.length;
    }

    public int frameCount() {
        return channels[0].length;
    }

    public int sampleRate() {
        return sampleRate;
    }

    public int bitDepth() {
        return bitDepth;
    }

    /** Direct view of the backing arrays — {@code data()[channel][frame]}. */
    public int[][] data() {
        return channels;
    }

    public int getSample(int channel, int frame) {
        return channels[channel][frame];
    }

    public void setSample(int channel, int frame, int value) {
        channels[channel][frame] = value;
    }

    public AudioBuffer copy() {
        int[][] copyOf = new int[channels.length][];
        for (int c = 0; c < channels.length; c++) {
            copyOf[c] = Arrays.copyOf(channels[c], channels[c].length);
        }
        return new AudioBuffer(copyOf, sampleRate, bitDepth);
    }

    public void copyFrom(AudioBuffer source) {
        Objects.requireNonNull(source, "source");
        if (source.channelCount() != channelCount() || source.frameCount() != frameCount()) {
            throw new IllegalArgumentException(
                    "cannot copy from " + source.channelCount() + "ch x " + source.frameCount()
                            + " into " + channelCount() + "ch x " + frameCount());
        }
        for (int c = 0; c < channels.length; c++) {
            System.arraycopy(source.channels[c], 0, channels[c], 0, channels[c].length);
        }
    }

    public double durationSeconds() {
        return frameCount() / (double) sampleRate;
    }
}
