package com.alaishat.mohammad.pixellab.features.colorspace.usecase;

import com.alaishat.mohammad.pixellab.domain.color.Cmyk;
import com.alaishat.mohammad.pixellab.domain.color.ColorSpace;
import com.alaishat.mohammad.pixellab.domain.color.ColorTriplet;
import com.alaishat.mohammad.pixellab.domain.color.conversion.RgbCmyk;
import com.alaishat.mohammad.pixellab.domain.color.conversion.RgbHsv;
import com.alaishat.mohammad.pixellab.domain.color.conversion.RgbLab;
import com.alaishat.mohammad.pixellab.domain.color.conversion.RgbYCbCr;
import com.alaishat.mohammad.pixellab.domain.color.conversion.RgbYuv;
import com.alaishat.mohammad.pixellab.domain.image.PixelBuffer;

/**
 * Visualization of an RGB image in a different color space.
 *
 * Each pixel's RGB triple is converted to the target space, then the three
 * components are scaled into [0, 255] and packed into the R / G / B slots of
 * the output buffer for display. This is the standard "show the channels as
 * an image" view used in the color-space teaching context.
 *
 * <p>CMYK is the odd one out — it has 4 channels but the display buffer has 3
 * slots, so K is dropped from the visualization. The canvas isn't authoritative
 * here; the working buffer in {@link com.alaishat.mohammad.pixellab.domain.image.EditSession}
 * remains the RGB ground truth, so switching back to RGB always shows the
 * original image.
 */
public final class ConvertColorSpaceUseCase {

    public PixelBuffer execute(PixelBuffer rgbBuffer, ColorSpace target) {
        if (target == ColorSpace.RGB) {
            return rgbBuffer.copy();
        }

        int w = rgbBuffer.width();
        int h = rgbBuffer.height();
        PixelBuffer out = new PixelBuffer(w, h);
        int[] in = rgbBuffer.data();
        int[] dst = out.data();

        for (int i = 0; i < in.length; i++) {
            int argb = in[i];
            int alpha = (argb >>> 24) & 0xFF;
            double r = ((argb >>> 16) & 0xFF) / 255.0;
            double g = ((argb >>> 8) & 0xFF) / 255.0;
            double b = (argb & 0xFF) / 255.0;
            ColorTriplet rgb = new ColorTriplet(r, g, b);

            int c0, c1, c2;
            switch (target) {
                case HSV -> {
                    ColorTriplet hsv = RgbHsv.toHsv(rgb);
                    c0 = clamp255(hsv.a() / 360.0 * 255.0);
                    c1 = clamp255(hsv.b() * 255.0);
                    c2 = clamp255(hsv.c() * 255.0);
                }
                case YUV -> {
                    ColorTriplet yuv = RgbYuv.toYuv(rgb);
                    // U ~ [-0.436, 0.436], V ~ [-0.615, 0.615] — bias to [0, 1] for display.
                    c0 = clamp255(yuv.a() * 255.0);
                    c1 = clamp255((yuv.b() + 0.5) * 255.0);
                    c2 = clamp255((yuv.c() + 0.5) * 255.0);
                }
                case YCBCR -> {
                    ColorTriplet ycc = RgbYCbCr.toYCbCr(rgb);
                    c0 = clamp255(ycc.a() * 255.0);
                    c1 = clamp255(ycc.b() * 255.0);
                    c2 = clamp255(ycc.c() * 255.0);
                }
                case LAB -> {
                    ColorTriplet lab = RgbLab.toLab(rgb);
                    c0 = clamp255(lab.a() / 100.0 * 255.0);
                    c1 = clamp255(lab.b() + 128.0);
                    c2 = clamp255(lab.c() + 128.0);
                }
                case CMYK -> {
                    Cmyk cmyk = RgbCmyk.toCmyk(rgb);
                    c0 = clamp255(cmyk.c() * 255.0);
                    c1 = clamp255(cmyk.m() * 255.0);
                    c2 = clamp255(cmyk.y() * 255.0);
                }
                default -> throw new IllegalArgumentException("Unsupported target: " + target);
            }

            dst[i] = (alpha << 24) | (c0 << 16) | (c1 << 8) | c2;
        }
        return out;
    }

    private static int clamp255(double v) {
        if (v <= 0.0) return 0;
        if (v >= 255.0) return 255;
        return (int) Math.round(v);
    }
}
