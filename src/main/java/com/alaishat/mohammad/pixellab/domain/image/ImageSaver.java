package com.alaishat.mohammad.pixellab.domain.image;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Domain port for writing a PixelBuffer to disk in a named image format.
 * Implemented by infrastructure (e.g. ImageIO) so the domain stays UI-/IO-agnostic.
 *
 * @see com.alaishat.mohammad.pixellab.infrastructure.io.FileSystemImageSaver
 */
public interface ImageSaver {
    /**
     * @param format short format name as recognized by the implementation
     *               (e.g. "PNG", "JPEG", "BMP"). Case-insensitive.
     */
    void save(PixelBuffer pixels, Path target, String format) throws IOException;
}
