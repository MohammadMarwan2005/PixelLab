package com.alaishat.mohammad.pixellab.features.editsession.usecase;

import com.alaishat.mohammad.pixellab.domain.image.EditSession;
import com.alaishat.mohammad.pixellab.domain.image.ImageSaver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class SaveAsImageUseCase {

    private final ImageSaver saver;

    public SaveAsImageUseCase(ImageSaver saver) {
        this.saver = Objects.requireNonNull(saver, "saver");
    }

    public void execute(EditSession session, Path target, String format) throws IOException {
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(format, "format");
        saver.save(session.workingBuffer(), target, format);
    }
}
