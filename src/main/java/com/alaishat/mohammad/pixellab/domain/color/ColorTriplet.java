package com.alaishat.mohammad.pixellab.domain.color;

/**
 * Three normalized color components. The interpretation of {@code a, b, c} depends on the
 * color space the conversion class is operating in (e.g. RGB → r,g,b ∈ [0,1];
 * HSV → h ∈ [0,360), s,v ∈ [0,1]; LAB → L ∈ [0,100], a/b roughly [-128,127]).
 */
public record ColorTriplet(double a, double b, double c) {
    public static ColorTriplet of(double a, double b, double c) {
        return new ColorTriplet(a, b, c);
    }
}
