package com.alaishat.mohammad.pixellab.features.channels.usecase;

import com.alaishat.mohammad.pixellab.domain.color.ColorSpace;
import com.alaishat.mohammad.pixellab.domain.image.PixelBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Returns N grayscale buffers (one per channel of the target space) where each
 * pixel's intensity equals that channel's value scaled into [0, 255] (Phase 6.1).
 * For CMYK this returns 4 buffers; for the rest, 3.
 */
public final class SplitChannelsUseCase {

    public List<PixelBuffer> execute(PixelBuffer rgbBuffer, ColorSpace space) {
        int components = space.componentCount();
        int w = rgbBuffer.width();
        int h = rgbBuffer.height();
        List<PixelBuffer> result = new ArrayList<>(components);
        int[][] dsts = new int[components][];
        for (int c = 0; c < components; c++) {
            PixelBuffer buf = new PixelBuffer(w, h);
            result.add(buf);
            dsts[c] = buf.data();
        }

        int[] in = rgbBuffer.data();
        for (int i = 0; i < in.length; i++) {
            int argb = in[i];
            int alpha = (argb >>> 24) & 0xFF;
            double r = ((argb >>> 16) & 0xFF) / 255.0;
            double g = ((argb >>> 8) & 0xFF) / 255.0;
            double b = (argb & 0xFF) / 255.0;
            double[] channels = ChannelCodec.decompose(space, r, g, b);
            for (int c = 0; c < components; c++) {
                int gray = clamp255(toGray(space, c, channels[c]));
                dsts[c][i] = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
            }
        }
        return result;
    }

    /** Maps a single channel value to the [0, 255] gray scale used by the thumbnail. */
    private static double toGray(ColorSpace space, int channelIndex, double value) {
        return switch (space) {
            case RGB, CMYK, YCBCR -> value * 255.0;
            case HSV -> channelIndex == 0 ? value / 360.0 * 255.0 : value * 255.0;
            case YUV -> switch (channelIndex) {
                case 0 -> value * 255.0;
                case 1 -> (value + 0.5) * 255.0;
                case 2 -> (value + 0.5) * 255.0;
                default -> value * 255.0;
            };
            case LAB -> channelIndex == 0
                    ? value / 100.0 * 255.0
                    : value + 128.0;
        };
    }

    private static int clamp255(double v) {
        if (v <= 0.0) return 0;
        if (v >= 255.0) return 255;
        return (int) Math.round(v);
    }
}
