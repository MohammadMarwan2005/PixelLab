package com.alaishat.mohammad.pixellab.domain.image;

import java.util.Arrays;
import java.util.Objects;

/**
 * Mutable container for a 2D grid of ARGB pixels backed by a single int[].
 *
 * Pixel layout per int (top-down, left-to-right; row-major):
 *   bits 24..31 : alpha
 *   bits 16..23 : red
 *   bits  8..15 : green
 *   bits  0.. 7 : blue
 *
 * The PixelBuffer is the one mutable value type in the domain (Section 5 of the plan):
 * pixel-loop use cases need direct int[] access for performance, so we expose the array
 * via {@link #data()} rather than copying. Callers are expected to honor the contract.
 */
public final class PixelBuffer {

    private final int width;
    private final int height;
    private final int[] argb;

    public PixelBuffer(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("PixelBuffer dimensions must be positive: " + width + "x" + height);
        }
        this.width = width;
        this.height = height;
        this.argb = new int[width * height];
    }

    public PixelBuffer(int width, int height, int[] argb) {
        this(width, height);
        if (argb.length != this.argb.length) {
            throw new IllegalArgumentException(
                    "argb length " + argb.length + " does not match " + width + "x" + height);
        }
        System.arraycopy(argb, 0, this.argb, 0, argb.length);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int pixelCount() {
        return argb.length;
    }

    /** Direct view of the backing array. Mutating this mutates the buffer — by design. */
    public int[] data() {
        return argb;
    }

    public int getArgb(int x, int y) {
        return argb[index(x, y)];
    }

    public void setArgb(int x, int y, int argb) {
        this.argb[index(x, y)] = argb;
    }

    public Pixel getPixel(int x, int y) {
        return Pixel.fromArgb(argb[index(x, y)]);
    }

    public void setPixel(int x, int y, Pixel pixel) {
        argb[index(x, y)] = pixel.toArgb();
    }

    public PixelBuffer copy() {
        return new PixelBuffer(width, height, argb);
    }

    public void copyFrom(PixelBuffer source) {
        Objects.requireNonNull(source, "source");
        if (source.width != width || source.height != height) {
            throw new IllegalArgumentException(
                    "Cannot copy from " + source.width + "x" + source.height
                            + " into " + width + "x" + height);
        }
        System.arraycopy(source.argb, 0, argb, 0, argb.length);
    }

    private int index(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IndexOutOfBoundsException("Pixel (" + x + ", " + y + ") outside " + width + "x" + height);
        }
        return y * width + x;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PixelBuffer other)) return false;
        return width == other.width && height == other.height && Arrays.equals(argb, other.argb);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, Arrays.hashCode(argb));
    }
}