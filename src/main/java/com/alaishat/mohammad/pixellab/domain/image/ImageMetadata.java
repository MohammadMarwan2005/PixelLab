package com.alaishat.mohammad.pixellab.domain.image;

/**
 * Display metadata about an image loaded into the workspace.
 *
 * @param name      original file name (with extension), e.g. "photo.jpg"
 * @param format    short format identifier, e.g. "PNG", "JPEG", "BMP"
 * @param fileSize  size of the source file in bytes
 * @param width     width in pixels
 * @param height    height in pixels
 */
public record ImageMetadata(String name, String format, long fileSize, int width, int height) {

    public ImageMetadata {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (format == null || format.isBlank()) {
            throw new IllegalArgumentException("format must not be blank");
        }
        if (fileSize < 0) {
            throw new IllegalArgumentException("fileSize must be >= 0: " + fileSize);
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("dimensions must be positive: " + width + "x" + height);
        }
    }
}