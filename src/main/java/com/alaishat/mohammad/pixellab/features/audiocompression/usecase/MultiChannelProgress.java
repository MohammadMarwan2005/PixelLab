package com.alaishat.mohammad.pixellab.features.audiocompression.usecase;

import com.alaishat.mohammad.pixellab.domain.audio.compression.CompressionProgressListener;

/**
 * {@link com.alaishat.mohammad.pixellab.domain.audio.compression.AudioCodec}
 * reports progress in terms of "samples processed in this
 * channel's loop". Compression/decompression runs that loop once per channel,
 * so this rescales each channel's local progress into one continuous
 * processed/total pair spanning every channel — what the use cases' callers
 * (the {@code Task} driving the progress bar) actually want to see.
 */
final class MultiChannelProgress {

    private MultiChannelProgress() {}

    static CompressionProgressListener scoped(CompressionProgressListener delegate,
                                               int channelIndex, int channelCount, long framesPerChannel) {
        long offset = (long) channelIndex * framesPerChannel;
        long total = (long) channelCount * framesPerChannel;
        return new CompressionProgressListener() {
            @Override
            public void onProgress(long processed, long ignoredLocalTotal) {
                delegate.onProgress(offset + processed, total);
            }

            @Override
            public boolean isCancelled() {
                return delegate.isCancelled();
            }
        };
    }
}
