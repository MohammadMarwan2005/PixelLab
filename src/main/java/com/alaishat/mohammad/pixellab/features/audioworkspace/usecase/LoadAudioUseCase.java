package com.alaishat.mohammad.pixellab.features.audioworkspace.usecase;

import com.alaishat.mohammad.pixellab.domain.audio.AudioLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class LoadAudioUseCase {

    private final AudioLoader loader;

    public LoadAudioUseCase(AudioLoader loader) {
        this.loader = Objects.requireNonNull(loader, "loader");
    }

    public AudioLoader.LoadedAudio execute(Path source) throws IOException {
        Objects.requireNonNull(source, "source");
        return loader.load(source);
    }
}
