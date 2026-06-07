package com.alaishat.mohammad.pixellab.infrastructure.io;

import com.alaishat.mohammad.pixellab.domain.audio.AudioBuffer;
import com.alaishat.mohammad.pixellab.domain.audio.AudioSaver;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Writes an {@link AudioBuffer} to disk as PCM audio via {@code javax.sound.sampled}.
 * Mirrors {@link FileSystemImageSaver}: wrap the in-memory representation in
 * the JDK's container type and let {@code AudioSystem} do the encoding.
 */
public final class FileSystemAudioSaver implements AudioSaver {

    @Override
    public void save(AudioBuffer audio, Path target, String format) throws IOException {
        AudioFormat audioFormat = PcmInterop.toAudioFormat(audio);
        byte[] raw = PcmInterop.toInterleavedBytes(audio);

        try (AudioInputStream stream = new AudioInputStream(
                new ByteArrayInputStream(raw), audioFormat, audio.frameCount())) {
            AudioSystem.write(stream, resolveFileType(format), target.toFile());
        }
    }

    private static AudioFileFormat.Type resolveFileType(String format) {
        return switch (format.toUpperCase(Locale.ROOT)) {
            case "AIFF", "AIFC" -> AudioFileFormat.Type.AIFF;
            case "AU" -> AudioFileFormat.Type.AU;
            default -> AudioFileFormat.Type.WAVE;
        };
    }
}
