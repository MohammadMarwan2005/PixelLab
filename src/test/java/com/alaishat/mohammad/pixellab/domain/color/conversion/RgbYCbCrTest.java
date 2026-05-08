package com.alaishat.mohammad.pixellab.domain.color.conversion;

import com.alaishat.mohammad.pixellab.domain.color.ColorTriplet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RgbYCbCrTest {

    private static final double KNOWN_TOL = 1e-3;
    /** JPEG-rounded BT.601 coefficients drift ~1e-7 on the round trip. */
    private static final double ROUND_TRIP_TOL = 1e-6;

    @Test
    void knownValues_blackWhiteAndChromaCenter() {
        ColorTriplet black = RgbYCbCr.toYCbCr(new ColorTriplet(0, 0, 0));
        assertEquals(0.0, black.a(), KNOWN_TOL);
        assertEquals(0.5, black.b(), KNOWN_TOL);
        assertEquals(0.5, black.c(), KNOWN_TOL);

        ColorTriplet white = RgbYCbCr.toYCbCr(new ColorTriplet(1, 1, 1));
        assertEquals(1.0, white.a(), KNOWN_TOL);
        assertEquals(0.5, white.b(), KNOWN_TOL);
        assertEquals(0.5, white.c(), KNOWN_TOL);

        ColorTriplet gray = RgbYCbCr.toYCbCr(new ColorTriplet(0.5, 0.5, 0.5));
        assertEquals(0.5, gray.a(), KNOWN_TOL);
        assertEquals(0.5, gray.b(), KNOWN_TOL);
        assertEquals(0.5, gray.c(), KNOWN_TOL);
    }

    @Test
    void roundTrip_samplesFromAcrossTheGamut() {
        for (ColorTriplet rgb : ColorSamples.RGB_SAMPLES) {
            ColorTriplet back = RgbYCbCr.toRgb(RgbYCbCr.toYCbCr(rgb));
            assertEquals(rgb.a(), back.a(), ROUND_TRIP_TOL, "r mismatch for " + rgb);
            assertEquals(rgb.b(), back.b(), ROUND_TRIP_TOL, "g mismatch for " + rgb);
            assertEquals(rgb.c(), back.c(), ROUND_TRIP_TOL, "b mismatch for " + rgb);
        }
    }
}
