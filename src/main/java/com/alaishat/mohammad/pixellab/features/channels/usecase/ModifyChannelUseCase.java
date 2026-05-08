package com.alaishat.mohammad.pixellab.features.channels.usecase;

import com.alaishat.mohammad.pixellab.domain.color.ColorSpace;
import com.alaishat.mohammad.pixellab.domain.image.PixelBuffer;

import java.util.Arrays;
import java.util.Objects;

/**
 * Modifies a single channel of the working buffer — adds offset, multiplies, or
 * sets to a fixed value (Phase 6.2). Other channels pass through unchanged.
 */
public final class ModifyChannelUseCase {

    private final ApplyChannelAdjustmentsUseCase delegate;

    public ModifyChannelUseCase(ApplyChannelAdjustmentsUseCase delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    public PixelBuffer execute(PixelBuffer rgbBuffer, ColorSpace space, int channel,
                               ChannelOperation operation, double value) {
        if (channel < 0 || channel >= space.componentCount()) {
            throw new IllegalArgumentException("Bad channel " + channel + " for " + space.displayName());
        }
        ChannelAdjustment[] adjustments = new ChannelAdjustment[space.componentCount()];
        Arrays.fill(adjustments, ChannelAdjustment.noOp());
        adjustments[channel] = new ChannelAdjustment(true, operation, value);
        return delegate.execute(rgbBuffer, space, adjustments);
    }
}
