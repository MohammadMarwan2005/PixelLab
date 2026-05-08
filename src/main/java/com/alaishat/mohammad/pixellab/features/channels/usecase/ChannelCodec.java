package com.alaishat.mohammad.pixellab.features.channels.usecase;

import com.alaishat.mohammad.pixellab.domain.color.Cmyk;
import com.alaishat.mohammad.pixellab.domain.color.ColorSpace;
import com.alaishat.mohammad.pixellab.domain.color.ColorTriplet;
import com.alaishat.mohammad.pixellab.domain.color.conversion.RgbCmyk;
import com.alaishat.mohammad.pixellab.domain.color.conversion.RgbHsv;
import com.alaishat.mohammad.pixellab.domain.color.conversion.RgbLab;
import com.alaishat.mohammad.pixellab.domain.color.conversion.RgbYCbCr;
import com.alaishat.mohammad.pixellab.domain.color.conversion.RgbYuv;

/**
 * Helper shared by the channel-manipulation use cases and the view layer.
 *
 * <p>Decomposes a normalized RGB pixel into the components of a target color
 * space and recomposes them back. Also exposes per-channel "natural ranges"
 * so the UI can offer sensible slider scales without each call site
 * hard-coding ranges.
 */
public final class ChannelCodec {

    private ChannelCodec() {}

    static double[] decompose(ColorSpace space, double r, double g, double b) {
        ColorTriplet rgb = new ColorTriplet(r, g, b);
        return switch (space) {
            case RGB -> new double[] { r, g, b };
            case HSV -> {
                ColorTriplet hsv = RgbHsv.toHsv(rgb);
                yield new double[] { hsv.a(), hsv.b(), hsv.c() };
            }
            case YUV -> {
                ColorTriplet yuv = RgbYuv.toYuv(rgb);
                yield new double[] { yuv.a(), yuv.b(), yuv.c() };
            }
            case YCBCR -> {
                ColorTriplet ycc = RgbYCbCr.toYCbCr(rgb);
                yield new double[] { ycc.a(), ycc.b(), ycc.c() };
            }
            case LAB -> {
                ColorTriplet lab = RgbLab.toLab(rgb);
                yield new double[] { lab.a(), lab.b(), lab.c() };
            }
            case CMYK -> {
                Cmyk cmyk = RgbCmyk.toCmyk(rgb);
                yield new double[] { cmyk.c(), cmyk.m(), cmyk.y(), cmyk.k() };
            }
        };
    }

    static double[] recomposeRgb(ColorSpace space, double[] channels) {
        ColorTriplet rgb = switch (space) {
            case RGB -> new ColorTriplet(channels[0], channels[1], channels[2]);
            case HSV -> RgbHsv.toRgb(new ColorTriplet(channels[0], channels[1], channels[2]));
            case YUV -> RgbYuv.toRgb(new ColorTriplet(channels[0], channels[1], channels[2]));
            case YCBCR -> RgbYCbCr.toRgb(new ColorTriplet(channels[0], channels[1], channels[2]));
            case LAB -> RgbLab.toRgb(new ColorTriplet(channels[0], channels[1], channels[2]));
            case CMYK -> RgbCmyk.toRgb(new Cmyk(channels[0], channels[1], channels[2], channels[3]));
        };
        return new double[] { rgb.a(), rgb.b(), rgb.c() };
    }

    /** Natural display range for a given channel — used by sliders to scale offsets. */
    public static double naturalRange(ColorSpace space, int channelIndex) {
        return switch (space) {
            case RGB, CMYK, YCBCR -> 1.0;
            case HSV -> channelIndex == 0 ? 360.0 : 1.0;
            case YUV -> switch (channelIndex) {
                case 0 -> 1.0;
                case 1 -> 0.872;     // U: full range ~[-0.436, +0.436]
                case 2 -> 1.230;     // V: full range ~[-0.615, +0.615]
                default -> throw new IllegalArgumentException("Bad YUV channel: " + channelIndex);
            };
            case LAB -> channelIndex == 0 ? 100.0 : 256.0;  // a, b ≈ [-128, 127]
        };
    }
}
