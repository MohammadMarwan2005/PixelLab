package com.alaishat.mohammad.pixellab.domain.audio.compression;

/**
 * Reads individual bits MSB-first from a byte array — the mirror of {@link BitWriter}.
 */
final class BitReader {

    private final byte[] data;
    private int bytePos;
    private int bitPos;

    BitReader(byte[] data) {
        this.data = data;
    }

    int readBit() {
        int bit = (data[bytePos] >>> (7 - bitPos)) & 1;
        bitPos++;
        if (bitPos == 8) {
            bitPos = 0;
            bytePos++;
        }
        return bit;
    }

    /** Reads {@code bitCount} bits and reassembles them MSB first into an int. */
    int readBits(int bitCount) {
        int value = 0;
        for (int i = 0; i < bitCount; i++) {
            value = (value << 1) | readBit();
        }
        return value;
    }
}
