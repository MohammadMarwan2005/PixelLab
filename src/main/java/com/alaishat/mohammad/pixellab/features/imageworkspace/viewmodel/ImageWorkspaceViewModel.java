package com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel;

import com.alaishat.mohammad.pixellab.domain.image.EditSession;
import com.alaishat.mohammad.pixellab.domain.image.ImageLoader;
import com.alaishat.mohammad.pixellab.domain.image.ImageMetadata;
import com.alaishat.mohammad.pixellab.domain.image.PixelBuffer;
import com.alaishat.mohammad.pixellab.features.imageworkspace.usecase.LoadImageUseCase;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class ImageWorkspaceViewModel {

    private final LoadImageUseCase loadImage;

    private final ObjectProperty<EditSession> editSession = new SimpleObjectProperty<>();
    private final ObjectProperty<PixelBuffer> currentBuffer = new SimpleObjectProperty<>();
    private final ObjectProperty<ImageMetadata> currentMetadata = new SimpleObjectProperty<>();
    private final ObjectProperty<Path> currentSource = new SimpleObjectProperty<>();
    private final ReadOnlyObjectWrapper<Throwable> lastError = new ReadOnlyObjectWrapper<>();

    public ImageWorkspaceViewModel(LoadImageUseCase loadImage) {
        this.loadImage = Objects.requireNonNull(loadImage, "loadImage");
    }

    public ObjectProperty<EditSession> editSessionProperty() {
        return editSession;
    }

    public ObjectProperty<PixelBuffer> currentBufferProperty() {
        return currentBuffer;
    }

    public ObjectProperty<ImageMetadata> currentMetadataProperty() {
        return currentMetadata;
    }

    public ObjectProperty<Path> currentSourceProperty() {
        return currentSource;
    }

    public ReadOnlyObjectProperty<Throwable> lastErrorProperty() {
        return lastError.getReadOnlyProperty();
    }

    public BooleanBinding hasImageBinding() {
        return Bindings.isNotNull(editSession);
    }

    public void open(Path source) {
        try {
            ImageLoader.LoadedImage loaded = loadImage.execute(source);
            EditSession session = new EditSession(loaded.pixels(), source, loaded.metadata().format());
            editSession.set(session);
            currentBuffer.set(session.workingBuffer());
            currentMetadata.set(loaded.metadata());
            // Set source last so listeners observing it (e.g. recents) see the
            // metadata + buffer already in place when they fire.
            currentSource.set(source);
            lastError.set(null);
        } catch (IOException | RuntimeException e) {
            lastError.set(e);
        }
    }

    /**
     * Re-publishes the current session's working buffer to {@link #currentBufferProperty()}.
     * Edit use cases that replace the working buffer (Reset, Convert color space, …) call
     * this so the canvas re-renders.
     */
    public void republishWorkingBuffer() {
        EditSession session = editSession.get();
        if (session != null) {
            currentBuffer.set(session.workingBuffer());
        }
    }
}
