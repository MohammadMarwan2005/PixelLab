package com.alaishat.mohammad.pixellab.domain.audio;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Domain-level abstraction for reading an audio file off disk into the working
 * representation ({@link AudioBuffer} + {@link AudioMetadata}). Implemented by
 * infrastructure (Java Sound) so the domain stays pure.
 *
 * @see com.alaishat.mohammad.pixellab.infrastructure.io.FileSystemAudioLoader
 */
public interface AudioLoader {
    LoadedAudio load(Path source) throws IOException;

    record LoadedAudio(AudioBuffer audio, AudioMetadata metadata) {}
}
