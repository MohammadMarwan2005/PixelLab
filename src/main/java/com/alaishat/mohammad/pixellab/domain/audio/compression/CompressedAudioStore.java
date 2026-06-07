package com.alaishat.mohammad.pixellab.domain.audio.compression;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Domain port for persisting/reading back {@link EncodedAudio} (Req. 11).
 *
 * <p>None of PixelLab's three algorithms produce a standard-container-compatible
 * bitstream (their codes are sub-byte / variable bit width), so a small custom
 * container is unavoidable — every real codec (FLAC, MP3, ...) does the same,
 * just with a far more elaborate header. PixelLab's container ("PXAC") is a
 * fixed-size header carrying exactly what {@link AudioCodec#decode} needs
 * (algorithm id, format, frame count, settings) followed by the packed
 * per-channel bitstreams.
 *
 * @see com.alaishat.mohammad.pixellab.infrastructure.io.FileSystemCompressedAudioStore
 */
public interface CompressedAudioStore {

    String FILE_EXTENSION = "pxac";

    void save(EncodedAudio encoded, Path target) throws IOException;

    EncodedAudio load(Path source) throws IOException;
}
