package com.alaishat.mohammad.pixellab.domain.color.conversion;

import com.alaishat.mohammad.pixellab.domain.color.ColorTriplet;

/**
 * RGB ↔ YCbCr — full-range JPEG variant of BT.601.
 * RGB in [0,1]; Y, Cb, Cr in [0,1] (Cb/Cr are centered at 0.5, neutral chroma).
 */
public final class RgbYCbCr {

    private RgbYCbCr() {}

    public static ColorTriplet toYCbCr(ColorTriplet rgb) {
        double r = rgb.a();
        double g = rgb.b();
        double b = rgb.c();

        double y  =  0.299     * r + 0.587     * g + 0.114     * b;
        double cb = -0.168736  * r - 0.331264  * g + 0.5       * b + 0.5;
        double cr =  0.5       * r - 0.418688  * g - 0.081312  * b + 0.5;
        return new ColorTriplet(y, cb, cr);
    }

    public static ColorTriplet toRgb(ColorTriplet ycbcr) {
        double y  = ycbcr.a();
        double cb = ycbcr.b() - 0.5;
        double cr = ycbcr.c() - 0.5;

        double r = y + 1.402    * cr;
        double g = y - 0.344136 * cb - 0.714136 * cr;
        double b = y + 1.772    * cb;
        return new ColorTriplet(r, g, b);
    }
}
