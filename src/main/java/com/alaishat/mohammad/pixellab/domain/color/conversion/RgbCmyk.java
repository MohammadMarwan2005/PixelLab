package com.alaishat.mohammad.pixellab.domain.color.conversion;

import com.alaishat.mohammad.pixellab.domain.color.Cmyk;
import com.alaishat.mohammad.pixellab.domain.color.ColorTriplet;

/**
 * RGB ↔ CMYK. RGB and CMYK components all in [0, 1].
 * Pure black (R=G=B=0) maps to K=1 with C=M=Y=0.
 */
public final class RgbCmyk {

    private RgbCmyk() {}

    public static Cmyk toCmyk(ColorTriplet rgb) {
        double r = rgb.a();
        double g = rgb.b();
        double b = rgb.c();

        double k = 1.0 - Math.max(r, Math.max(g, b));
        if (k >= 1.0) {
            return new Cmyk(0.0, 0.0, 0.0, 1.0);
        }
        double inv = 1.0 - k;
        double c = (1.0 - r - k) / inv;
        double m = (1.0 - g - k) / inv;
        double y = (1.0 - b - k) / inv;
        return new Cmyk(c, m, y, k);
    }

    public static ColorTriplet toRgb(Cmyk cmyk) {
        double inv = 1.0 - cmyk.k();
        double r = (1.0 - cmyk.c()) * inv;
        double g = (1.0 - cmyk.m()) * inv;
        double b = (1.0 - cmyk.y()) * inv;
        return new ColorTriplet(r, g, b);
    }
}
