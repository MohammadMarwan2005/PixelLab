package com.alaishat.mohammad.pixellab.domain.color.conversion;

import com.alaishat.mohammad.pixellab.domain.color.ColorTriplet;

/**
 * RGB ↔ YUV (BT.601 analog).
 * RGB in [0,1]; Y in [0,1]; U in ~[-0.436, 0.436]; V in ~[-0.615, 0.615].
 */
public final class RgbYuv {

    private RgbYuv() {}

    public static ColorTriplet toYuv(ColorTriplet rgb) {
        double r = rgb.a();
        double g = rgb.b();
        double b = rgb.c();

        double y =  0.299 * r + 0.587 * g + 0.114 * b;
        double u = -0.14713 * r - 0.28886 * g + 0.436 * b;
        double v =  0.615 * r - 0.51499 * g - 0.10001 * b;
        return new ColorTriplet(y, u, v);
    }

    public static ColorTriplet toRgb(ColorTriplet yuv) {
        double y = yuv.a();
        double u = yuv.b();
        double v = yuv.c();

        double r = y + 1.13983 * v;
        double g = y - 0.39465 * u - 0.58060 * v;
        double b = y + 2.03211 * u;
        return new ColorTriplet(r, g, b);
    }
}
