package com.alaishat.mohammad.pixellab.infrastructure.io;

import com.alaishat.mohammad.pixellab.domain.image.ImageLoader;
import com.alaishat.mohammad.pixellab.domain.image.ImageMetadata;
import com.alaishat.mohammad.pixellab.domain.image.PixelBuffer;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public final class FileSystemImageLoader implements ImageLoader {

    @Override
    public LoadedImage load(Path source) throws IOException {
        long fileSize = Files.size(source);
        String fileName = source.getFileName().toString();

        try (ImageInputStream stream = ImageIO.createImageInputStream(source.toFile())) {
            if (stream == null) {
                throw new IOException("Could not open image stream for: " + source);
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                throw new IOException("No image reader for: " + source + " (unsupported format?)");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(stream, true, true);
                String format = reader.getFormatName().toUpperCase();
                BufferedImage image = reader.read(0);
                PixelBuffer pixels = toPixelBuffer(image);
                ImageMetadata metadata = new ImageMetadata(
                        fileName, format, fileSize, pixels.width(), pixels.height());
                return new LoadedImage(pixels, metadata);
            } finally {
                reader.dispose();
            }
        }
    }

    private static PixelBuffer toPixelBuffer(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        // BufferedImage#getRGB always delivers packed ARGB regardless of the source's
        // internal type, so this works uniformly for PNG/JPEG/BMP/GIF.
        int[] argb = new int[w * h];
        image.getRGB(0, 0, w, h, argb, 0, w);
        if (image.getColorModel().hasAlpha()) {
            return new PixelBuffer(w, h, argb);
        }
        // Sources without alpha (e.g. JPEG) still return alpha=255 from getRGB,
        // but be defensive in case getRGB ever returns 0 for the alpha byte.
        for (int i = 0; i < argb.length; i++) {
            argb[i] |= 0xFF000000;
        }
        return new PixelBuffer(w, h, argb);
    }
}
