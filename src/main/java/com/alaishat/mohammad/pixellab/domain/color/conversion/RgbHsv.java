package com.alaishat.mohammad.pixellab.domain.color.conversion;

import com.alaishat.mohammad.pixellab.domain.color.ColorTriplet;

/**
 * RGB ↔ HSV.
 * RGB components in [0, 1]; H in [0, 360), S and V in [0, 1].
 */
public final class RgbHsv {

    private RgbHsv() {}

    public static ColorTriplet toHsv(ColorTriplet rgb) {
        double r = rgb.a();
        double g = rgb.b();
        double b = rgb.c();

        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double delta = max - min;

        double h;
        if (delta == 0.0) {
            h = 0.0;
        } else if (max == r) {
            h = 60.0 * (((g - b) / delta) % 6.0);
        } else if (max == g) {
            h = 60.0 * (((b - r) / delta) + 2.0);
        } else {
            h = 60.0 * (((r - g) / delta) + 4.0);
        }
        if (h < 0.0) h += 360.0;

        double s = (max == 0.0) ? 0.0 : delta / max;
        double v = max;

        return new ColorTriplet(h, s, v);
    }

    public static ColorTriplet toRgb(ColorTriplet hsv) {
        double h = hsv.a();
        double s = hsv.b();
        double v = hsv.c();

        double c = v * s;
        double hPrime = (h % 360.0) / 60.0;
        double x = c * (1.0 - Math.abs((hPrime % 2.0) - 1.0));
        double m = v - c;

        double r, g, b;
        if (hPrime < 1.0)      { r = c; g = x; b = 0; }
        else if (hPrime < 2.0) { r = x; g = c; b = 0; }
        else if (hPrime < 3.0) { r = 0; g = c; b = x; }
        else if (hPrime < 4.0) { r = 0; g = x; b = c; }
        else if (hPrime < 5.0) { r = x; g = 0; b = c; }
        else                   { r = c; g = 0; b = x; }

        return new ColorTriplet(r + m, g + m, b + m);
    }
}
