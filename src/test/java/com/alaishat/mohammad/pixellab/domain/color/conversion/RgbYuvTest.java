package com.alaishat.mohammad.pixellab.domain.color.conversion;

import com.alaishat.mohammad.pixellab.domain.color.ColorTriplet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RgbYuvTest {

    private static final double KNOWN_TOL = 1e-3;
    /** Published BT.601 coefficients are rounded, so round-trip drifts a few µ. */
    private static final double ROUND_TRIP_TOL = 1e-4;

    @Test
    void knownValues_blackWhiteY() {
        ColorTriplet black = RgbYuv.toYuv(new ColorTriplet(0, 0, 0));
        assertEquals(0.0, black.a(), KNOWN_TOL);
        assertEquals(0.0, black.b(), KNOWN_TOL);
        assertEquals(0.0, black.c(), KNOWN_TOL);

        ColorTriplet white = RgbYuv.toYuv(new ColorTriplet(1, 1, 1));
        assertEquals(1.0, white.a(), KNOWN_TOL);
        assertEquals(0.0, white.b(), KNOWN_TOL);
        assertEquals(0.0, white.c(), KNOWN_TOL);

        ColorTriplet red = RgbYuv.toYuv(new ColorTriplet(1, 0, 0));
        assertEquals(0.299, red.a(), KNOWN_TOL);
    }

    @Test
    void roundTrip_samplesFromAcrossTheGamut() {
        for (ColorTriplet rgb : ColorSamples.RGB_SAMPLES) {
            ColorTriplet back = RgbYuv.toRgb(RgbYuv.toYuv(rgb));
            assertEquals(rgb.a(), back.a(), ROUND_TRIP_TOL, "r mismatch for " + rgb);
            assertEquals(rgb.b(), back.b(), ROUND_TRIP_TOL, "g mismatch for " + rgb);
            assertEquals(rgb.c(), back.c(), ROUND_TRIP_TOL, "b mismatch for " + rgb);
        }
    }
}
