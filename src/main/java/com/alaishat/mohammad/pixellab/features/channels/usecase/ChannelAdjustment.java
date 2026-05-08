package com.alaishat.mohammad.pixellab.features.channels.usecase;

/**
 * What to do to one channel during a multi-channel reconstruction pass. Disabling
 * is modeled separately so it overrides any operation/value (zero takes priority).
 */
public record ChannelAdjustment(boolean enabled, ChannelOperation operation, double value) {

    public static ChannelAdjustment noOp() {
        return new ChannelAdjustment(true, ChannelOperation.OFFSET, 0.0);
    }

    public static ChannelAdjustment disabled() {
        return new ChannelAdjustment(false, ChannelOperation.OFFSET, 0.0);
    }

    public static ChannelAdjustment offset(double value) {
        return new ChannelAdjustment(true, ChannelOperation.OFFSET, value);
    }

    public static ChannelAdjustment multiply(double factor) {
        return new ChannelAdjustment(true, ChannelOperation.MULTIPLY, factor);
    }

    public static ChannelAdjustment setTo(double value) {
        return new ChannelAdjustment(true, ChannelOperation.SET, value);
    }
}
