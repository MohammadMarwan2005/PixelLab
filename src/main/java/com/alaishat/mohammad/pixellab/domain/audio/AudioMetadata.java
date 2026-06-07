package com.alaishat.mohammad.pixellab.domain.audio;

/**
 * Display metadata about an audio file loaded into the workspace (Req. 3).
 *
 * @param name            original file name (with extension), e.g. "song.wav"
 * @param encoding        short encoding identifier, e.g. "PCM_SIGNED"
 * @param fileSize        size of the source file in bytes
 * @param durationSeconds playback duration in seconds
 * @param sampleRate      samples per second per channel, e.g. 44100
 * @param channelCount    number of audio channels (1 = mono, 2 = stereo)
 * @param bitDepth        bits per sample, e.g. 16
 * @param bitRate         bits per second: sampleRate * bitDepth * channelCount
 */
public record AudioMetadata(
        String name,
        String encoding,
        long fileSize,
        double durationSeconds,
        int sampleRate,
        int channelCount,
        int bitDepth,
        long bitRate
) {

    public AudioMetadata {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (encoding == null || encoding.isBlank()) {
            throw new IllegalArgumentException("encoding must not be blank");
        }
        if (fileSize < 0) {
            throw new IllegalArgumentException("fileSize must be >= 0: " + fileSize);
        }
        if (sampleRate <= 0 || channelCount <= 0 || bitDepth <= 0) {
            throw new IllegalArgumentException(
                    "sampleRate, channelCount and bitDepth must be positive");
        }
    }

    public static AudioMetadata of(String name, String encoding, long fileSize, AudioBuffer audio) {
        int sampleRate = audio.sampleRate();
        int channelCount = audio.channelCount();
        int bitDepth = audio.bitDepth();
        return new AudioMetadata(
                name,
                encoding,
                fileSize,
                audio.durationSeconds(),
                sampleRate,
                channelCount,
                bitDepth,
                (long) sampleRate * bitDepth * channelCount);
    }
}
