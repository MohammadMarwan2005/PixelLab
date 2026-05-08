package com.alaishat.mohammad.pixellab.domain.color.conversion;

import com.alaishat.mohammad.pixellab.domain.color.ColorTriplet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RgbHsvTest {

    private static final double TOL = 1e-9;
    private static final double ROUND_TRIP_TOL = 1e-9;

    @Test
    void knownValues_blackWhitePrimaries() {
        ColorTriplet hsvBlack = RgbHsv.toHsv(new ColorTriplet(0, 0, 0));
        assertEquals(0.0, hsvBlack.a(), TOL);
        assertEquals(0.0, hsvBlack.b(), TOL);
        assertEquals(0.0, hsvBlack.c(), TOL);

        ColorTriplet hsvWhite = RgbHsv.toHsv(new ColorTriplet(1, 1, 1));
        assertEquals(0.0, hsvWhite.a(), TOL);
        assertEquals(0.0, hsvWhite.b(), TOL);
        assertEquals(1.0, hsvWhite.c(), TOL);

        ColorTriplet hsvRed = RgbHsv.toHsv(new ColorTriplet(1, 0, 0));
        assertEquals(0.0,   hsvRed.a(), TOL);
        assertEquals(1.0,   hsvRed.b(), TOL);
        assertEquals(1.0,   hsvRed.c(), TOL);

        ColorTriplet hsvGreen = RgbHsv.toHsv(new ColorTriplet(0, 1, 0));
        assertEquals(120.0, hsvGreen.a(), TOL);
        assertEquals(1.0,   hsvGreen.b(), TOL);
        assertEquals(1.0,   hsvGreen.c(), TOL);

        ColorTriplet hsvBlue = RgbHsv.toHsv(new ColorTriplet(0, 0, 1));
        assertEquals(240.0, hsvBlue.a(), TOL);
        assertEquals(1.0,   hsvBlue.b(), TOL);
        assertEquals(1.0,   hsvBlue.c(), TOL);
    }

    @Test
    void roundTrip_samplesFromAcrossTheGamut() {
        for (ColorTriplet rgb : ColorSamples.RGB_SAMPLES) {
            ColorTriplet back = RgbHsv.toRgb(RgbHsv.toHsv(rgb));
            assertEquals(rgb.a(), back.a(), ROUND_TRIP_TOL, "r mismatch for " + rgb);
            assertEquals(rgb.b(), back.b(), ROUND_TRIP_TOL, "g mismatch for " + rgb);
            assertEquals(rgb.c(), back.c(), ROUND_TRIP_TOL, "b mismatch for " + rgb);
        }
    }
}
