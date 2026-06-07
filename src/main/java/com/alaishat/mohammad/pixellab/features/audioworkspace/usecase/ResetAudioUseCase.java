package com.alaishat.mohammad.pixellab.features.audioworkspace.usecase;

import com.alaishat.mohammad.pixellab.domain.audio.AudioEditSession;

import java.util.Objects;

/**
 * Discards in-flight edits (a compress→decompress round trip) by replacing the
 * working buffer with a fresh copy of the original (Req. 9). Mirrors {@code
 * com.alaishat.mohammad.pixellab.features.editsession.usecase.ResetUseCase}.
 */
public final class ResetAudioUseCase {

    public void execute(AudioEditSession session) {
        Objects.requireNonNull(session, "session");
        session.replaceWorking(session.originalBuffer().copy());
    }
}
