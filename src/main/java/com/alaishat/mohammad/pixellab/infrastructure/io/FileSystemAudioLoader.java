package com.alaishat.mohammad.pixellab.infrastructure.io;

import com.alaishat.mohammad.pixellab.domain.audio.AudioBuffer;
import com.alaishat.mohammad.pixellab.domain.audio.AudioLoader;
import com.alaishat.mohammad.pixellab.domain.audio.AudioMetadata;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads a WAV/AIFF/AU file via {@code javax.sound.sampled} and decodes it to
 * 16-bit signed PCM regardless of the source's actual sample size or
 * signedness — the audio equivalent of {@code BufferedImage#getRGB} always
 * delivering packed ARGB on the image side (see {@link FileSystemImageLoader}).
 *
 * <p>Built-in Java Sound only — no extra Maven dependency, and it covers what
 * the assignment needs (raw PCM in, so the compression algorithms have
 * meaningful uncompressed samples to work on).
 */
public final class FileSystemAudioLoader implements AudioLoader {

    private static final int INTERNAL_BIT_DEPTH = 16;

    @Override
    public LoadedAudio load(Path source) throws IOException {
        long fileSize = Files.size(source);
        String fileName = source.getFileName().toString();

        try (AudioInputStream sourceStream = AudioSystem.getAudioInputStream(source.toFile())) {
            AudioFormat sourceFormat = sourceStream.getFormat();
            String encodingName = sourceFormat.getEncoding().toString();

            AudioFormat pcmFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sourceFormat.getSampleRate(),
                    INTERNAL_BIT_DEPTH,
                    sourceFormat.getChannels(),
                    sourceFormat.getChannels() * (INTERNAL_BIT_DEPTH / 8),
                    sourceFormat.getSampleRate(),
                    false);

            try (AudioInputStream pcmStream = AudioSystem.getAudioInputStream(pcmFormat, sourceStream)) {
                byte[] raw = pcmStream.readAllBytes();
                AudioBuffer audio = PcmInterop.fromInterleavedBytes(
                        raw, pcmFormat.getChannels(), (int) pcmFormat.getSampleRate(), INTERNAL_BIT_DEPTH);
                AudioMetadata metadata = AudioMetadata.of(fileName, encodingName, fileSize, audio);
                return new LoadedAudio(audio, metadata);
            }
        } catch (UnsupportedAudioFileException e) {
            throw new IOException("Unsupported or non-PCM audio file: " + source, e);
        }
    }
}
