package com.alaishat.mohammad.pixellab.domain.color.conversion;

import com.alaishat.mohammad.pixellab.domain.color.ColorTriplet;

/**
 * RGB ↔ CIE Lab. Goes through sRGB linearization and the D65 XYZ white point.
 *
 * Input/output sRGB components are in [0, 1].
 * L is in [0, 100]; a and b roughly in [-128, 127].
 */
public final class RgbLab {

    private RgbLab() {}

    private static final double XN = 0.95047;
    private static final double YN = 1.00000;
    private static final double ZN = 1.08883;

    private static final double EPSILON = 216.0 / 24389.0;
    private static final double KAPPA   =  24389.0 / 27.0;

    public static ColorTriplet toLab(ColorTriplet rgb) {
        double r = srgbToLinear(rgb.a());
        double g = srgbToLinear(rgb.b());
        double b = srgbToLinear(rgb.c());

        double x = 0.4124564 * r + 0.3575761 * g + 0.1804375 * b;
        double y = 0.2126729 * r + 0.7151522 * g + 0.0721750 * b;
        double z = 0.0193339 * r + 0.1191920 * g + 0.9503041 * b;

        double fx = labF(x / XN);
        double fy = labF(y / YN);
        double fz = labF(z / ZN);

        double l = 116.0 * fy - 16.0;
        double aa = 500.0 * (fx - fy);
        double bb = 200.0 * (fy - fz);
        return new ColorTriplet(l, aa, bb);
    }

    public static ColorTriplet toRgb(ColorTriplet lab) {
        double l = lab.a();
        double aa = lab.b();
        double bb = lab.c();

        double fy = (l + 16.0) / 116.0;
        double fx = aa / 500.0 + fy;
        double fz = fy - bb / 200.0;

        double x = XN * labFInverse(fx);
        double y = YN * labFInverse(fy);
        double z = ZN * labFInverse(fz);

        double rLin =  3.2404542 * x - 1.5371385 * y - 0.4985314 * z;
        double gLin = -0.9692660 * x + 1.8760108 * y + 0.0415560 * z;
        double bLin =  0.0556434 * x - 0.2040259 * y + 1.0572252 * z;

        return new ColorTriplet(linearToSrgb(rLin), linearToSrgb(gLin), linearToSrgb(bLin));
    }

    private static double srgbToLinear(double c) {
        return (c <= 0.04045) ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    private static double linearToSrgb(double c) {
        return (c <= 0.0031308) ? 12.92 * c : 1.055 * Math.pow(c, 1.0 / 2.4) - 0.055;
    }

    private static double labF(double t) {
        return (t > EPSILON) ? Math.cbrt(t) : (KAPPA * t + 16.0) / 116.0;
    }

    private static double labFInverse(double t) {
        double t3 = t * t * t;
        return (t3 > EPSILON) ? t3 : (116.0 * t - 16.0) / KAPPA;
    }
}
