package com.alaishat.mohammad.pixellab.domain.color.conversion;

import com.alaishat.mohammad.pixellab.domain.color.ColorTriplet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RgbLabTest {

    /** D65 reference Y (1.00000) is approximated; Lab L for white drifts ~4e-6. */
    private static final double KNOWN_TOL = 1e-4;
    /** sRGB linearization + 3x3 matrix + cbrt + inverse stack drifts ~2e-6. */
    private static final double ROUND_TRIP_TOL = 1e-4;

    @Test
    void knownValues_d65WhiteAndBlack() {
        ColorTriplet white = RgbLab.toLab(new ColorTriplet(1, 1, 1));
        assertEquals(100.0, white.a(), KNOWN_TOL);
        assertEquals(  0.0, white.b(), KNOWN_TOL);
        assertEquals(  0.0, white.c(), KNOWN_TOL);

        ColorTriplet black = RgbLab.toLab(new ColorTriplet(0, 0, 0));
        assertEquals(0.0, black.a(), KNOWN_TOL);
        assertEquals(0.0, black.b(), KNOWN_TOL);
        assertEquals(0.0, black.c(), KNOWN_TOL);
    }

    @Test
    void roundTrip_samplesFromAcrossTheGamut() {
        for (ColorTriplet rgb : ColorSamples.RGB_SAMPLES) {
            ColorTriplet back = RgbLab.toRgb(RgbLab.toLab(rgb));
            assertEquals(rgb.a(), back.a(), ROUND_TRIP_TOL, "r mismatch for " + rgb);
            assertEquals(rgb.b(), back.b(), ROUND_TRIP_TOL, "g mismatch for " + rgb);
            assertEquals(rgb.c(), back.c(), ROUND_TRIP_TOL, "b mismatch for " + rgb);
        }
    }
}
