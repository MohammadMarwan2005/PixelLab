package com.alaishat.mohammad.pixellab.features.editsession.usecase;

import com.alaishat.mohammad.pixellab.domain.image.EditSession;
import com.alaishat.mohammad.pixellab.domain.image.ImageSaver;

import java.io.IOException;
import java.util.Objects;

/**
 * Overwrites the original file with the current working buffer, in the same
 * format the file was loaded as.
 */
public final class SaveImageUseCase {

    private final ImageSaver saver;

    public SaveImageUseCase(ImageSaver saver) {
        this.saver = Objects.requireNonNull(saver, "saver");
    }

    public void execute(EditSession session) throws IOException {
        Objects.requireNonNull(session, "session");
        saver.save(session.workingBuffer(), session.sourcePath(), session.originalFormat());
    }
}
