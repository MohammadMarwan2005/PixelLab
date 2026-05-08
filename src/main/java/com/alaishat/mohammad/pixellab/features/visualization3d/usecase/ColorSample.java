package com.alaishat.mohammad.pixellab.features.visualization3d.usecase;

import com.alaishat.mohammad.pixellab.domain.color.ColorSpace;
import com.alaishat.mohammad.pixellab.domain.color.ColorTriplet;

import javafx.geometry.Point3D;

/**
 * One sampled color used in the 3D visualization (Phase 8).
 *
 * @param space     the color space being visualized
 * @param position  3D position the sample sits at (roughly within [-1, 1]³)
 * @param rgb       sample's color expressed as normalized RGB in [0, 1] — used both
 *                  for rendering the sphere and for the color picker
 * @param channels  raw channel values in the source space (length 3 or 4 for CMYK)
 */
public record ColorSample(ColorSpace space, Point3D position, ColorTriplet rgb, double[] channels) {
}
