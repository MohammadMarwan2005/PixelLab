package com.alaishat.mohammad.pixellab.features.editsession.usecase;

import com.alaishat.mohammad.pixellab.domain.image.EditSession;

import java.util.Objects;

/**
 * Discards in-flight edits by replacing the working buffer with a fresh copy of
 * the original. Replacing the reference (rather than mutating in place) makes the
 * change visible to JavaFX bindings on the working-buffer property.
 */
public final class ResetUseCase {

    public void execute(EditSession session) {
        Objects.requireNonNull(session, "session");
        session.replaceWorking(session.originalBuffer().copy());
    }
}
