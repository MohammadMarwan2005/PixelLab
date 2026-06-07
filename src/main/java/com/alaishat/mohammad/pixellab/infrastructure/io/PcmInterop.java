package com.alaishat.mohammad.pixellab.infrastructure.io;

import com.alaishat.mohammad.pixellab.domain.audio.AudioBuffer;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Conversions between {@link AudioBuffer} (per-channel signed sample arrays)
 * and the interleaved little-endian PCM byte layout / {@link AudioFormat} that
 * {@code javax.sound.sampled} speaks. Shared by the loader, saver, and player —
 * each would otherwise duplicate this exact byte-shuffling.
 *
 * <p>PixelLab always works internally in 16-bit signed PCM — the same
 * "normalize on the way in" choice the image side makes by always decoding to
 * packed ARGB regardless of source format.
 */
public final class PcmInterop {

    private PcmInterop() {}

    public static AudioFormat toAudioFormat(AudioBuffer audio) {
        int bytesPerSample = audio.bitDepth() / 8;
        return new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                audio.sampleRate(),
                audio.bitDepth(),
                audio.channelCount(),
                audio.channelCount() * bytesPerSample,
                audio.sampleRate(),
                false);
    }

    /** Packs {@code audio}'s per-channel samples into interleaved 16-bit little-endian bytes. */
    public static byte[] toInterleavedBytes(AudioBuffer audio) {
        int channelCount = audio.channelCount();
        int frameCount = audio.frameCount();
        int[][] channels = audio.data();

        byte[] raw = new byte[frameCount * channelCount * 2];
        ByteBuffer buffer = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        for (int frame = 0; frame < frameCount; frame++) {
            for (int channel = 0; channel < channelCount; channel++) {
                buffer.putShort((short) channels[channel][frame]);
            }
        }
        return raw;
    }

    /** Unpacks interleaved 16-bit little-endian bytes into a per-channel {@link AudioBuffer}. */
    public static AudioBuffer fromInterleavedBytes(byte[] raw, int channelCount, int sampleRate, int bitDepth) {
        int bytesPerFrame = channelCount * (bitDepth / 8);
        int frameCount = raw.length / bytesPerFrame;

        int[][] channels = new int[channelCount][frameCount];
        ByteBuffer buffer = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        for (int frame = 0; frame < frameCount; frame++) {
            for (int channel = 0; channel < channelCount; channel++) {
                channels[channel][frame] = buffer.getShort();
            }
        }
        return new AudioBuffer(channels, sampleRate, bitDepth);
    }
}
