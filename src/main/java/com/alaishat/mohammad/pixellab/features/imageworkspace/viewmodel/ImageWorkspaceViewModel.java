package com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel;

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

    private final ObjectProperty<PixelBuffer> currentBuffer = new SimpleObjectProperty<>();
    private final ObjectProperty<ImageMetadata> currentMetadata = new SimpleObjectProperty<>();
    private final ReadOnlyObjectWrapper<Throwable> lastError = new ReadOnlyObjectWrapper<>();

    public ImageWorkspaceViewModel(LoadImageUseCase loadImage) {
        this.loadImage = Objects.requireNonNull(loadImage, "loadImage");
    }

    public ObjectProperty<PixelBuffer> currentBufferProperty() {
        return currentBuffer;
    }

    public ObjectProperty<ImageMetadata> currentMetadataProperty() {
        return currentMetadata;
    }

    public ReadOnlyObjectProperty<Throwable> lastErrorProperty() {
        return lastError.getReadOnlyProperty();
    }

    public BooleanBinding hasImageBinding() {
        return Bindings.isNotNull(currentBuffer);
    }

    public void open(Path source) {
        try {
            ImageLoader.LoadedImage loaded = loadImage.execute(source);
            currentBuffer.set(loaded.pixels());
            currentMetadata.set(loaded.metadata());
            lastError.set(null);
        } catch (IOException | RuntimeException e) {
            lastError.set(e);
        }
    }
}
