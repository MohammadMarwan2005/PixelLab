package com.alaishat.mohammad.pixellab.infrastructure.io;

import com.alaishat.mohammad.pixellab.domain.audio.compression.CompressedAudioStore;
import com.alaishat.mohammad.pixellab.domain.audio.compression.CompressionAlgorithm;
import com.alaishat.mohammad.pixellab.domain.audio.compression.CompressionSettings;
import com.alaishat.mohammad.pixellab.domain.audio.compression.EncodedAudio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads/writes PixelLab's custom "PXAC" container — see
 * {@link CompressedAudioStore} for why a custom format is unavoidable here.
 *
 * <p>Layout (version 1), all multi-byte fields big-endian via {@link DataOutputStream}:
 * <pre>
 *   magic            4 bytes  ASCII "PXAC"
 *   version          1 byte
 *   algorithm id     1 byte   ({@link CompressionAlgorithm#ordinal()})
 *   sampleRate       4 bytes  int
 *   bitDepth         1 byte
 *   channelCount     1 byte
 *   frameCount       4 bytes  int
 *   quantizationBits 1 byte
 *   stepSize         8 bytes  double
 *   adaptationFactor 8 bytes  double
 *   per channel:     4 bytes  byte-length, then that many encoded bitstream bytes
 * </pre>
 */
public final class FileSystemCompressedAudioStore implements CompressedAudioStore {

    private static final byte[] MAGIC = {'P', 'X', 'A', 'C'};
    private static final int VERSION = 1;

    @Override
    public void save(EncodedAudio encoded, Path target) throws IOException {
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(target)))) {
            out.write(MAGIC);
            out.writeByte(VERSION);
            out.writeByte(encoded.algorithm().ordinal());
            out.writeInt(encoded.sampleRate());
            out.writeByte(encoded.bitDepth());
            out.writeByte(encoded.channelCount());
            out.writeInt(encoded.frameCount());

            CompressionSettings settings = encoded.settings();
            out.writeByte(settings.quantizationBits());
            out.writeDouble(settings.stepSize());
            out.writeDouble(settings.adaptationFactor());

            for (byte[] channel : encoded.channels()) {
                out.writeInt(channel.length);
                out.write(channel);
            }
        }
    }

    @Override
    public EncodedAudio load(Path source) throws IOException {
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(source)))) {
            byte[] magic = new byte[MAGIC.length];
            in.readFully(magic);
            if (!java.util.Arrays.equals(magic, MAGIC)) {
                throw new IOException("Not a PixelLab compressed-audio file: " + source);
            }
            int version = in.readUnsignedByte();
            if (version != VERSION) {
                throw new IOException("Unsupported .pxac version " + version + " in " + source);
            }

            CompressionAlgorithm algorithm = CompressionAlgorithm.values()[in.readUnsignedByte()];
            int sampleRate = in.readInt();
            int bitDepth = in.readUnsignedByte();
            int channelCount = in.readUnsignedByte();
            int frameCount = in.readInt();

            int quantizationBits = in.readUnsignedByte();
            double stepSize = in.readDouble();
            double adaptationFactor = in.readDouble();
            CompressionSettings settings = new CompressionSettings(quantizationBits, stepSize, adaptationFactor);

            byte[][] channels = new byte[channelCount][];
            for (int c = 0; c < channelCount; c++) {
                int length = in.readInt();
                byte[] channel = new byte[length];
                in.readFully(channel);
                channels[c] = channel;
            }

            return new EncodedAudio(algorithm, settings, sampleRate, bitDepth, frameCount, channels);
        }
    }
}
