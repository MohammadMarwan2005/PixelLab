package com.alaishat.mohammad.pixellab.domain.color;

/**
 * The six color spaces PixelLab supports (Requirement 2 of PROJECT_PLAN.md).
 *
 * Each space defines its own component labels and ranges; the conversion math
 * lives in {@code domain.color.conversion}, not here.
 */
public enum ColorSpace {
    RGB(  "RGB",   "R", "G", "B"),
    CMYK( "CMYK",  "C", "M", "Y", "K"),
    HSV(  "HSV",   "H", "S", "V"),
    YUV(  "YUV",   "Y", "U", "V"),
    LAB(  "LAB",   "L", "a", "b"),
    YCBCR("YCbCr", "Y", "Cb", "Cr");

    private final String displayName;
    private final String[] componentLabels;

    ColorSpace(String displayName, String... componentLabels) {
        this.displayName = displayName;
        this.componentLabels = componentLabels;
    }

    public String displayName() {
        return displayName;
    }

    public int componentCount() {
        return componentLabels.length;
    }

    public String componentLabel(int index) {
        return componentLabels[index];
    }
}
