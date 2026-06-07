package com.alaishat.mohammad.pixellab.domain.audio.compression;

import java.io.ByteArrayOutputStream;

/**
 * Accumulates individual bits MSB-first into a byte array. Shared by every
 * codec — Delta/Adaptive-Delta Modulation write one bit per sample, DPCM
 * writes {@code quantizationBits} bits per sample.
 */
final class BitWriter {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private int currentByte;
    private int bitsInCurrentByte;

    void writeBit(int bit) {
        currentByte = (currentByte << 1) | (bit & 1);
        bitsInCurrentByte++;
        if (bitsInCurrentByte == 8) {
            out.write(currentByte);
            currentByte = 0;
            bitsInCurrentByte = 0;
        }
    }

    /** Writes the {@code bitCount} least-significant bits of {@code value}, MSB first. */
    void writeBits(int value, int bitCount) {
        for (int i = bitCount - 1; i >= 0; i--) {
            writeBit((value >>> i) & 1);
        }
    }

    /** Flushes any partial trailing byte (zero-padded) and returns the packed bytes. */
    byte[] toByteArray() {
        if (bitsInCurrentByte > 0) {
            out.write(currentByte << (8 - bitsInCurrentByte));
            currentByte = 0;
            bitsInCurrentByte = 0;
        }
        return out.toByteArray();
    }
}
