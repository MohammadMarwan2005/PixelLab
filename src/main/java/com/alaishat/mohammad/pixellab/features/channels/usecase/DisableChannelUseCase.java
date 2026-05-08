package com.alaishat.mohammad.pixellab.features.channels.usecase;

import com.alaishat.mohammad.pixellab.domain.color.ColorSpace;
import com.alaishat.mohammad.pixellab.domain.image.PixelBuffer;

import java.util.Arrays;
import java.util.Objects;

/**
 * Zeroes out a single channel of the working buffer and reconstructs the result
 * back into RGB (Phase 6.3). Useful for "what does this image look like with no
 * blue?" style demonstrations.
 */
public final class DisableChannelUseCase {

    private final ApplyChannelAdjustmentsUseCase delegate;

    public DisableChannelUseCase(ApplyChannelAdjustmentsUseCase delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    public PixelBuffer execute(PixelBuffer rgbBuffer, ColorSpace space, int channel) {
        if (channel < 0 || channel >= space.componentCount()) {
            throw new IllegalArgumentException("Bad channel " + channel + " for " + space.displayName());
        }
        ChannelAdjustment[] adjustments = new ChannelAdjustment[space.componentCount()];
        Arrays.fill(adjustments, ChannelAdjustment.noOp());
        adjustments[channel] = ChannelAdjustment.disabled();
        return delegate.execute(rgbBuffer, space, adjustments);
    }
}
