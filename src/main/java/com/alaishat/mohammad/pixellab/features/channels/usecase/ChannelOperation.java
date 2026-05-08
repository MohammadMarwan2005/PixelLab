package com.alaishat.mohammad.pixellab.features.channels.usecase;

/**
 * The three modes of {@link com.alaishat.mohammad.pixellab.features.channels.usecase.ModifyChannelUseCase}
 * (Phase 6.2): {@code OFFSET} adds a value, {@code MULTIPLY} scales, {@code SET}
 * overwrites. Disabling a channel is handled separately by
 * {@link com.alaishat.mohammad.pixellab.features.channels.usecase.DisableChannelUseCase}.
 */
public enum ChannelOperation {
    OFFSET, MULTIPLY, SET
}
