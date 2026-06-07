package com.alaishat.mohammad.pixellab.features.audioworkspace.usecase;

import com.alaishat.mohammad.pixellab.domain.audio.AudioEditSession;
import com.alaishat.mohammad.pixellab.domain.audio.AudioSaver;

import java.io.IOException;
import java.util.Objects;

/**
 * Overwrites the original file with the current working buffer (Req. 11),
 * in the same format the file was loaded as.
 */
public final class SaveAudioUseCase {

    private final AudioSaver saver;

    public SaveAudioUseCase(AudioSaver saver) {
        this.saver = Objects.requireNonNull(saver, "saver");
    }

    public void execute(AudioEditSession session) throws IOException {
        Objects.requireNonNull(session, "session");
        saver.save(session.workingBuffer(), session.sourcePath(), session.originalFormat());
    }
}
