package com.alaishat.mohammad.pixellab.domain.color.conversion;

import com.alaishat.mohammad.pixellab.domain.color.ColorTriplet;

import java.util.List;

/**
 * Representative RGB samples used by every round-trip test. Covers the eight cube
 * vertices, mid-gray, and a mix of arbitrary values picked to hit non-trivial
 * branches in the conversion math (e.g. each HSV hue sextant; non-zero K in CMYK).
 */
final class ColorSamples {

    private ColorSamples() {}

    static final List<ColorTriplet> RGB_SAMPLES = List.of(
            new ColorTriplet(0.0, 0.0, 0.0),
            new ColorTriplet(1.0, 1.0, 1.0),
            new ColorTriplet(1.0, 0.0, 0.0),
            new ColorTriplet(0.0, 1.0, 0.0),
            new ColorTriplet(0.0, 0.0, 1.0),
            new ColorTriplet(1.0, 1.0, 0.0),
            new ColorTriplet(0.0, 1.0, 1.0),
            new ColorTriplet(1.0, 0.0, 1.0),
            new ColorTriplet(0.5, 0.5, 0.5),
            new ColorTriplet(0.25, 0.5, 0.75),
            new ColorTriplet(0.10, 0.20, 0.30),
            new ColorTriplet(0.90, 0.45, 0.10),
            new ColorTriplet(0.33, 0.66, 0.99)
    );
}
