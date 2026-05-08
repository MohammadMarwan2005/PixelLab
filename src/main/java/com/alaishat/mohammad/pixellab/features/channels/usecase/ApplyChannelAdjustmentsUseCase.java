package com.alaishat.mohammad.pixellab.features.channels.usecase;

import com.alaishat.mohammad.pixellab.domain.color.ColorSpace;
import com.alaishat.mohammad.pixellab.domain.image.PixelBuffer;

/**
 * Single-pass per-pixel reconstruction used by the channels view model for live
 * preview (Phase 6.6). Applies all channel adjustments in one decompose/recompose
 * round trip per pixel; the per-channel use cases ({@link ModifyChannelUseCase},
 * {@link DisableChannelUseCase}) are thin wrappers around this.
 */
public final class ApplyChannelAdjustmentsUseCase {

    public PixelBuffer execute(PixelBuffer rgbBuffer, ColorSpace space, ChannelAdjustment[] adjustments) {
        if (adjustments.length != space.componentCount()) {
            throw new IllegalArgumentException(
                    "Got " + adjustments.length + " adjustments for " + space.displayName()
                            + " (expected " + space.componentCount() + ")");
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

            double[] channels = ChannelCodec.decompose(space, r, g, b);
            for (int c = 0; c < channels.length; c++) {
                channels[c] = applyOne(channels[c], adjustments[c]);
            }
            double[] rgb = ChannelCodec.recomposeRgb(space, channels);
            int rr = clamp255(rgb[0] * 255.0);
            int gg = clamp255(rgb[1] * 255.0);
            int bb = clamp255(rgb[2] * 255.0);
            dst[i] = (alpha << 24) | (rr << 16) | (gg << 8) | bb;
        }
        return out;
    }

    private static double applyOne(double current, ChannelAdjustment adj) {
        if (!adj.enabled()) return 0.0;
        return switch (adj.operation()) {
            case OFFSET -> current + adj.value();
            case MULTIPLY -> current * adj.value();
            case SET -> adj.value();
        };
    }

    private static int clamp255(double v) {
        if (v <= 0.0) return 0;
        if (v >= 255.0) return 255;
        return (int) Math.round(v);
    }
}
