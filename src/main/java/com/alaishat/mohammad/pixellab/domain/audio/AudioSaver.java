package com.alaishat.mohammad.pixellab.domain.audio;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Domain port for writing an {@link AudioBuffer} to disk as a playable audio
 * file. Implemented by infrastructure (Java Sound) so the domain stays
 * UI-/IO-agnostic.
 *
 * @see com.alaishat.mohammad.pixellab.infrastructure.io.FileSystemAudioSaver
 */
public interface AudioSaver {
    /**
     * @param format short format name as recognized by the implementation
     *               (e.g. "WAV"). Case-insensitive.
     */
    void save(AudioBuffer audio, Path target, String format) throws IOException;
}
