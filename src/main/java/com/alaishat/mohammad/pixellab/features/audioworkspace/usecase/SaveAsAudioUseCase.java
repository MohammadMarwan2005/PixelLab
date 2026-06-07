package com.alaishat.mohammad.pixellab.features.audioworkspace.usecase;

import com.alaishat.mohammad.pixellab.domain.audio.AudioEditSession;
import com.alaishat.mohammad.pixellab.domain.audio.AudioSaver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class SaveAsAudioUseCase {

    private final AudioSaver saver;

    public SaveAsAudioUseCase(AudioSaver saver) {
        this.saver = Objects.requireNonNull(saver, "saver");
    }

    public void execute(AudioEditSession session, Path target, String format) throws IOException {
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(format, "format");
        saver.save(session.workingBuffer(), target, format);
    }
}
