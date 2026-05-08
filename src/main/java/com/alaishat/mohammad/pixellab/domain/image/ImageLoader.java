package com.alaishat.mohammad.pixellab.domain.image;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Domain-level abstraction for reading an image file off disk into the
 * working representation ({@link PixelBuffer} + {@link ImageMetadata}).
 * Implemented by infrastructure (e.g. ImageIO) so the domain stays pure.
 */
public interface ImageLoader {
    LoadedImage load(Path source) throws IOException;

    record LoadedImage(PixelBuffer pixels, ImageMetadata metadata) {}
}
