package com.alaishat.mohammad.pixellab.domain.image;

/**
 * A single ARGB pixel as 8-bit channels. Use {@link #toArgb()} / {@link #fromArgb(int)}
 * to round-trip with {@link PixelBuffer}'s packed int[] form.
 */
public record Pixel(int alpha, int red, int green, int blue) {

    public Pixel {
        check(alpha, "alpha");
        check(red, "red");
        check(green, "green");
        check(blue, "blue");
    }

    public static Pixel fromArgb(int argb) {
        return new Pixel(
                (argb >>> 24) & 0xFF,
                (argb >>> 16) & 0xFF,
                (argb >>> 8) & 0xFF,
                argb & 0xFF);
    }

    public static Pixel opaqueRgb(int red, int green, int blue) {
        return new Pixel(255, red, green, blue);
    }

    public int toArgb() {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static void check(int channel, String name) {
        if (channel < 0 || channel > 255) {
            throw new IllegalArgumentException(name + " out of range [0,255]: " + channel);
        }
    }
}