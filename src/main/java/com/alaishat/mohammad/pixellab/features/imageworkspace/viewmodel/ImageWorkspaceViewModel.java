package com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel;

import com.alaishat.mohammad.pixellab.domain.image.EditSession;
import com.alaishat.mohammad.pixellab.domain.image.ImageLoader;
import com.alaishat.mohammad.pixellab.domain.image.ImageMetadata;
import com.alaishat.mohammad.pixellab.domain.image.PixelBuffer;
import com.alaishat.mohammad.pixellab.features.imageworkspace.usecase.LoadImageUseCase;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class ImageWorkspaceViewModel {

    private final LoadImageUseCase loadImage;

    /** Source of truth for the loaded image and its working copy. */
    private final ObjectProperty<EditSession> editSession = new SimpleObjectProperty<>();
    /**
     * What the canvas renders. Updated by {@link
     * com.alaishat.mohammad.pixellab.features.colorspace.viewmodel.ColorSpaceViewModel}
     * — RGB equals the working buffer; non-RGB is a display-encoded conversion.
     */
    private final ObjectProperty<PixelBuffer> currentBuffer = new SimpleObjectProperty<>();
    private final ObjectProperty<ImageMetadata> currentMetadata = new SimpleObjectProperty<>();
    private final ObjectProperty<Path> currentSource = new SimpleObjectProperty<>();
    private final ReadOnlyObjectWrapper<Throwable> lastError = new ReadOnlyObjectWrapper<>();

    /**
     * Tick counter bumped whenever the working buffer is mutated or replaced.
     * Listeners (color space, channel manipulation, …) treat the bump as
     * "re-render now" — JavaFX object properties don't fire when the reference
     * stays the same, so we need an explicit signal for in-place mutations.
     */
    private final ReadOnlyIntegerWrapper workingBufferRevision = new ReadOnlyIntegerWrapper(0);

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

    public ReadOnlyIntegerProperty workingBufferRevisionProperty() {
        return workingBufferRevision.getReadOnlyProperty();
    }

    public BooleanBinding hasImageBinding() {
        return Bindings.isNotNull(editSession);
    }

    public void open(Path source) {
        try {
            ImageLoader.LoadedImage loaded = loadImage.execute(source);
            EditSession session = new EditSession(loaded.pixels(), source, loaded.metadata().format());
            editSession.set(session);
            // currentBuffer is published by ColorSpaceViewModel reacting to the editSession change.
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
     * Signals that the current session's working buffer has been replaced or
     * mutated. Use cases like Reset call this so observers (color space, future
     * channel/quantize features) recompute the displayed buffer.
     */
    public void republishWorkingBuffer() {
        workingBufferRevision.set(workingBufferRevision.get() + 1);
    }
}
