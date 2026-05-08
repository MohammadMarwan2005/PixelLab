package com.alaishat.mohammad.pixellab.domain.color.conversion;

import com.alaishat.mohammad.pixellab.domain.color.Cmyk;
import com.alaishat.mohammad.pixellab.domain.color.ColorTriplet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RgbCmykTest {

    private static final double TOL = 1e-9;

    @Test
    void knownValues() {
        Cmyk black = RgbCmyk.toCmyk(new ColorTriplet(0, 0, 0));
        assertEquals(0.0, black.c(), TOL);
        assertEquals(0.0, black.m(), TOL);
        assertEquals(0.0, black.y(), TOL);
        assertEquals(1.0, black.k(), TOL);

        Cmyk white = RgbCmyk.toCmyk(new ColorTriplet(1, 1, 1));
        assertEquals(0.0, white.c(), TOL);
        assertEquals(0.0, white.m(), TOL);
        assertEquals(0.0, white.y(), TOL);
        assertEquals(0.0, white.k(), TOL);

        Cmyk red = RgbCmyk.toCmyk(new ColorTriplet(1, 0, 0));
        assertEquals(0.0, red.c(), TOL);
        assertEquals(1.0, red.m(), TOL);
        assertEquals(1.0, red.y(), TOL);
        assertEquals(0.0, red.k(), TOL);
    }

    @Test
    void roundTrip_samplesFromAcrossTheGamut() {
        for (ColorTriplet rgb : ColorSamples.RGB_SAMPLES) {
            ColorTriplet back = RgbCmyk.toRgb(RgbCmyk.toCmyk(rgb));
            assertEquals(rgb.a(), back.a(), TOL, "r mismatch for " + rgb);
            assertEquals(rgb.b(), back.b(), TOL, "g mismatch for " + rgb);
            assertEquals(rgb.c(), back.c(), TOL, "b mismatch for " + rgb);
        }
    }
}
