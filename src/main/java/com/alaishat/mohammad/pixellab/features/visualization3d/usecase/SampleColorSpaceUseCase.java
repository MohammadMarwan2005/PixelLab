package com.alaishat.mohammad.pixellab.features.visualization3d.usecase;

import com.alaishat.mohammad.pixellab.domain.color.Cmyk;
import com.alaishat.mohammad.pixellab.domain.color.ColorSpace;
import com.alaishat.mohammad.pixellab.domain.color.ColorTriplet;
import com.alaishat.mohammad.pixellab.domain.color.conversion.RgbCmyk;
import com.alaishat.mohammad.pixellab.domain.color.conversion.RgbHsv;
import com.alaishat.mohammad.pixellab.domain.color.conversion.RgbLab;
import com.alaishat.mohammad.pixellab.domain.color.conversion.RgbYCbCr;
import com.alaishat.mohammad.pixellab.domain.color.conversion.RgbYuv;
import javafx.geometry.Point3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates sample points distributed inside each color space, mapped into a
 * unit-ish 3D box for the camera (Phase 8.3–8.5). Every sample carries:
 *   1. its position (where the sphere goes),
 *   2. the RGB color to render it with,
 *   3. the raw channel values in the source space (so the picker can show them).
 *
 * <p>The sampling resolution is the same per space ({@link #STEPS}^3); the
 * choice of {@code positionFor} determines the resulting shape (cube, cylinder,
 * skewed prism, etc).
 */
public final class SampleColorSpaceUseCase {

    /** Per-axis sample count. STEPS^3 spheres total. 11 = 1331 — denser, still pickable. */
    private static final int STEPS = 11;

    public List<ColorSample> execute(ColorSpace space) {
        return switch (space) {
            case RGB -> sampleRgb();
            case HSV -> sampleHsv();
            case CMYK -> sampleCmyk();
            case YUV -> sampleYuv();
            case YCBCR -> sampleYCbCr();
            case LAB -> sampleLab();
        };
    }

    private static List<ColorSample> sampleRgb() {
        List<ColorSample> out = new ArrayList<>(STEPS * STEPS * STEPS);
        for (int i = 0; i < STEPS; i++) {
            double r = i / (double) (STEPS - 1);
            for (int j = 0; j < STEPS; j++) {
                double g = j / (double) (STEPS - 1);
                for (int k = 0; k < STEPS; k++) {
                    double b = k / (double) (STEPS - 1);
                    Point3D pos = new Point3D(r * 2 - 1, g * 2 - 1, b * 2 - 1);
                    out.add(new ColorSample(ColorSpace.RGB, pos,
                            new ColorTriplet(r, g, b), new double[] { r, g, b }));
                }
            }
        }
        return out;
    }

    private static List<ColorSample> sampleHsv() {
        // Cylinder: H = angle, S = radius, V = height.
        List<ColorSample> out = new ArrayList<>();
        for (int i = 0; i < STEPS; i++) {
            double h = i / (double) STEPS * 360.0;     // 0..360 exclusive of 360
            for (int j = 0; j < STEPS; j++) {
                double s = j / (double) (STEPS - 1);
                for (int k = 0; k < STEPS; k++) {
                    double v = k / (double) (STEPS - 1);
                    double rad = Math.toRadians(h);
                    Point3D pos = new Point3D(s * Math.cos(rad), v * 2 - 1, s * Math.sin(rad));
                    ColorTriplet rgb = RgbHsv.toRgb(new ColorTriplet(h, s, v));
                    out.add(new ColorSample(ColorSpace.HSV, pos, rgb, new double[] { h, s, v }));
                }
            }
        }
        return out;
    }

    private static List<ColorSample> sampleCmyk() {
        // K = 0 plane: CMY cube, mirrored from RGB. (4D would need a slider.)
        List<ColorSample> out = new ArrayList<>(STEPS * STEPS * STEPS);
        for (int i = 0; i < STEPS; i++) {
            double c = i / (double) (STEPS - 1);
            for (int j = 0; j < STEPS; j++) {
                double m = j / (double) (STEPS - 1);
                for (int k = 0; k < STEPS; k++) {
                    double y = k / (double) (STEPS - 1);
                    Point3D pos = new Point3D(c * 2 - 1, m * 2 - 1, y * 2 - 1);
                    ColorTriplet rgb = RgbCmyk.toRgb(new Cmyk(c, m, y, 0.0));
                    out.add(new ColorSample(ColorSpace.CMYK, pos, rgb, new double[] { c, m, y, 0.0 }));
                }
            }
        }
        return out;
    }

    private static List<ColorSample> sampleYuv() {
        // Y stretches vertical; U / V are chroma plane.
        List<ColorSample> out = new ArrayList<>();
        for (int i = 0; i < STEPS; i++) {
            double yLuma = i / (double) (STEPS - 1);
            for (int j = 0; j < STEPS; j++) {
                double u = -0.436 + 0.872 * j / (STEPS - 1);
                for (int k = 0; k < STEPS; k++) {
                    double v = -0.615 + 1.230 * k / (STEPS - 1);
                    Point3D pos = new Point3D(u / 0.436, yLuma * 2 - 1, v / 0.615);
                    ColorTriplet rgb = RgbYuv.toRgb(new ColorTriplet(yLuma, u, v));
                    rgb = clampRgb(rgb);
                    out.add(new ColorSample(ColorSpace.YUV, pos, rgb, new double[] { yLuma, u, v }));
                }
            }
        }
        return out;
    }

    private static List<ColorSample> sampleYCbCr() {
        List<ColorSample> out = new ArrayList<>();
        for (int i = 0; i < STEPS; i++) {
            double y = i / (double) (STEPS - 1);
            for (int j = 0; j < STEPS; j++) {
                double cb = j / (double) (STEPS - 1);
                for (int k = 0; k < STEPS; k++) {
                    double cr = k / (double) (STEPS - 1);
                    Point3D pos = new Point3D((cb - 0.5) * 2, y * 2 - 1, (cr - 0.5) * 2);
                    ColorTriplet rgb = RgbYCbCr.toRgb(new ColorTriplet(y, cb, cr));
                    rgb = clampRgb(rgb);
                    out.add(new ColorSample(ColorSpace.YCBCR, pos, rgb, new double[] { y, cb, cr }));
                }
            }
        }
        return out;
    }

    private static List<ColorSample> sampleLab() {
        List<ColorSample> out = new ArrayList<>();
        for (int i = 0; i < STEPS; i++) {
            double l = i / (double) (STEPS - 1) * 100.0;
            for (int j = 0; j < STEPS; j++) {
                double a = -128.0 + 256.0 * j / (STEPS - 1);
                for (int k = 0; k < STEPS; k++) {
                    double b = -128.0 + 256.0 * k / (STEPS - 1);
                    Point3D pos = new Point3D(a / 128.0, (l - 50) / 50, b / 128.0);
                    ColorTriplet rgb = clampRgb(RgbLab.toRgb(new ColorTriplet(l, a, b)));
                    out.add(new ColorSample(ColorSpace.LAB, pos, rgb, new double[] { l, a, b }));
                }
            }
        }
        return out;
    }

    private static ColorTriplet clampRgb(ColorTriplet rgb) {
        return new ColorTriplet(clamp01(rgb.a()), clamp01(rgb.b()), clamp01(rgb.c()));
    }

    private static double clamp01(double v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }
}
