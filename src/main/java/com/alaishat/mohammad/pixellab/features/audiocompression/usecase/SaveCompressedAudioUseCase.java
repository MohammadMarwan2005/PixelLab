package com.alaishat.mohammad.pixellab.features.audiocompression.usecase;

import com.alaishat.mohammad.pixellab.domain.audio.compression.CompressedAudioStore;
import com.alaishat.mohammad.pixellab.domain.audio.compression.EncodedAudio;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Persists the most recent compression result as a "PXAC" container (Req. 11)
 * via {@link CompressedAudioStore}. Pure I/O delegation — mirrors {@code
 * features.audioworkspace.usecase.SaveAsAudioUseCase}.
 */
public final class SaveCompressedAudioUseCase {

    private final CompressedAudioStore store;

    public SaveCompressedAudioUseCase(CompressedAudioStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    public void execute(EncodedAudio encoded, Path target) throws IOException {
        Objects.requireNonNull(encoded, "encoded");
        Objects.requireNonNull(target, "target");
        store.save(encoded, target);
    }
}
