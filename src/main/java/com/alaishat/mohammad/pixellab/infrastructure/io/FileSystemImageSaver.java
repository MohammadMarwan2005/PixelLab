package com.alaishat.mohammad.pixellab.infrastructure.io;

import com.alaishat.mohammad.pixellab.domain.image.ImageSaver;
import com.alaishat.mohammad.pixellab.domain.image.PixelBuffer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

public final class FileSystemImageSaver implements ImageSaver {

    @Override
    public void save(PixelBuffer pixels, Path target, String format) throws IOException {
        String normalized = format.trim().toUpperCase(Locale.ROOT);
        BufferedImage image = toBufferedImage(pixels, normalized);

        // ImageIO format names: "png", "jpeg" (also "jpg"), "bmp", "gif".
        String writerHint = normalized.equals("JPG") ? "jpeg" : normalized.toLowerCase(Locale.ROOT);
        boolean ok = ImageIO.write(image, writerHint, target.toFile());
        if (!ok) {
            throw new IOException("No image writer available for format: " + format);
        }
    }

    private static BufferedImage toBufferedImage(PixelBuffer pixels, String format) {
        int w = pixels.width();
        int h = pixels.height();
        boolean preserveAlpha = format.equals("PNG") || format.equals("GIF");
        int type = preserveAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

        BufferedImage image = new BufferedImage(w, h, type);
        if (preserveAlpha) {
            image.setRGB(0, 0, w, h, pixels.data(), 0, w);
            return image;
        }
        // For RGB-only formats, drop alpha but keep RGB intact. We don't pre-multiply
        // because JPEG/BMP have no notion of alpha — passing the raw ARGB int with
        // alpha=255 (opaque) is fine, and our PixelBuffer always carries alpha=255
        // for sources without an alpha channel (see FileSystemImageLoader).
        image.setRGB(0, 0, w, h, pixels.data(), 0, w);
        return image;
    }
}
