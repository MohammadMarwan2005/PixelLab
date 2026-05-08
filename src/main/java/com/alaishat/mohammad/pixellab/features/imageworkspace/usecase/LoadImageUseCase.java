package com.alaishat.mohammad.pixellab.features.imageworkspace.usecase;

import com.alaishat.mohammad.pixellab.domain.image.ImageLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class LoadImageUseCase {

    private final ImageLoader loader;

    public LoadImageUseCase(ImageLoader loader) {
        this.loader = Objects.requireNonNull(loader, "loader");
    }

    public ImageLoader.LoadedImage execute(Path source) throws IOException {
        Objects.requireNonNull(source, "source");
        return loader.load(source);
    }
}
