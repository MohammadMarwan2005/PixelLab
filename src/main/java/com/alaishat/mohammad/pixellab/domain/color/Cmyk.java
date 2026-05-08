package com.alaishat.mohammad.pixellab.domain.color;

/**
 * Four normalized CMYK components, each in [0, 1]. Separate from {@link ColorTriplet}
 * because CMYK is the only 4-component space PixelLab supports.
 */
public record Cmyk(double c, double m, double y, double k) {
    public static Cmyk of(double c, double m, double y, double k) {
        return new Cmyk(c, m, y, k);
    }
}
