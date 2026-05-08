package com.alaishat.mohammad.pixellab.features.quantization.usecase;

import com.alaishat.mohammad.pixellab.domain.image.PixelBuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Color quantization via the classic median-cut algorithm (Phase 7.1, Req 7).
 *
 * <p>Steps:
 * <ol>
 *   <li>Put every pixel into one bucket.</li>
 *   <li>Until we have N buckets: pick the bucket with the largest spread along
 *       any of R/G/B, sort it on that axis, split at the median.</li>
 *   <li>Replace each pixel with the average color of its bucket.</li>
 * </ol>
 *
 * <p>Alpha is preserved per-pixel — only the RGB triples are quantized.
 */
public final class QuantizeColorsUseCase {

    public static final int MIN_COLORS = 1;
    public static final int MAX_COLORS = 256;

    public PixelBuffer execute(PixelBuffer buffer, int colorCount) {
        if (colorCount >= MAX_COLORS) {
            return buffer.copy();
        }
        int target = Math.max(MIN_COLORS, colorCount);

        int[] data = buffer.data();
        int n = data.length;
        if (n == 0) return buffer.copy();

        int[] alphas = new int[n];
        int[] reds = new int[n];
        int[] greens = new int[n];
        int[] blues = new int[n];
        for (int i = 0; i < n; i++) {
            int argb = data[i];
            alphas[i] = (argb >>> 24) & 0xFF;
            reds[i]   = (argb >>> 16) & 0xFF;
            greens[i] = (argb >>> 8)  & 0xFF;
            blues[i]  =  argb         & 0xFF;
        }

        Integer[] indices = new Integer[n];
        for (int i = 0; i < n; i++) indices[i] = i;

        List<Bucket> buckets = new ArrayList<>();
        buckets.add(new Bucket(0, n - 1));

        while (buckets.size() < target) {
            Bucket toSplit = null;
            int bestRange = 0;
            int bestAxis = 0;

            for (Bucket b : buckets) {
                if (b.size() < 2) continue;
                int[] range = b.range(indices, reds, greens, blues);
                int axis = 0;
                int spread = range[0];
                if (range[1] > spread) { axis = 1; spread = range[1]; }
                if (range[2] > spread) { axis = 2; spread = range[2]; }
                if (spread > bestRange) {
                    bestRange = spread;
                    toSplit = b;
                    bestAxis = axis;
                }
            }
            if (toSplit == null || bestRange == 0) break;

            int[] axisArr = (bestAxis == 0) ? reds : (bestAxis == 1) ? greens : blues;
            sortRange(indices, axisArr, toSplit.start, toSplit.end);

            int mid = (toSplit.start + toSplit.end) / 2;
            buckets.remove(toSplit);
            buckets.add(new Bucket(toSplit.start, mid));
            buckets.add(new Bucket(mid + 1, toSplit.end));
        }

        int[] paletteRgb = new int[buckets.size()];
        int[] pixelToBucket = new int[n];
        for (int b = 0; b < buckets.size(); b++) {
            Bucket bucket = buckets.get(b);
            long sumR = 0, sumG = 0, sumB = 0;
            int count = bucket.size();
            for (int i = bucket.start; i <= bucket.end; i++) {
                int pix = indices[i];
                sumR += reds[pix];
                sumG += greens[pix];
                sumB += blues[pix];
                pixelToBucket[pix] = b;
            }
            int avgR = (int) (sumR / count);
            int avgG = (int) (sumG / count);
            int avgB = (int) (sumB / count);
            paletteRgb[b] = (avgR << 16) | (avgG << 8) | avgB;
        }

        PixelBuffer out = new PixelBuffer(buffer.width(), buffer.height());
        int[] dst = out.data();
        for (int i = 0; i < n; i++) {
            int rgb = paletteRgb[pixelToBucket[i]];
            dst[i] = (alphas[i] << 24) | rgb;
        }
        return out;
    }

    private static void sortRange(Integer[] indices, int[] axisArr, int from, int to) {
        Comparator<Integer> byAxis = Comparator.comparingInt(a -> axisArr[a]);
        Arrays.sort(indices, from, to + 1, byAxis);
    }

    private static final class Bucket {
        final int start;
        final int end;

        Bucket(int start, int end) {
            this.start = start;
            this.end = end;
        }

        int size() {
            return end - start + 1;
        }

        int[] range(Integer[] indices, int[] r, int[] g, int[] b) {
            int rMin = 256, rMax = -1, gMin = 256, gMax = -1, bMin = 256, bMax = -1;
            for (int i = start; i <= end; i++) {
                int pix = indices[i];
                int rv = r[pix], gv = g[pix], bv = b[pix];
                if (rv < rMin) rMin = rv;
                if (rv > rMax) rMax = rv;
                if (gv < gMin) gMin = gv;
                if (gv > gMax) gMax = gv;
                if (bv < bMin) bMin = bv;
                if (bv > bMax) bMax = bv;
            }
            return new int[] { rMax - rMin, gMax - gMin, bMax - bMin };
        }
    }
}
