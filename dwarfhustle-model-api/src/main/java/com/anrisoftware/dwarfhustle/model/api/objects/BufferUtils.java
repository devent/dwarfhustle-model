package com.anrisoftware.dwarfhustle.model.api.objects;

import java.nio.ByteBuffer;

/**
 * Methods to read short, integer and long directly from a {@link ByteBuffer}.
 * No need to invoke {@link ByteBuffer#asShortBuffer()}, etc.
 */
public class BufferUtils {

    public static void writeShort(ByteBuffer b, short n) {
        b.put((byte) (n >> 8));
        b.put((byte) (n));
    }

    public static short readShort(ByteBuffer b) {
        return (short) ((b.get() & 255) << 8 | (b.get() & 255));
    }

    public static void writeInt(ByteBuffer b, int n) {
        b.put((byte) (n >> 24));
        b.put((byte) (n >> 16));
        b.put((byte) (n >> 8));
        b.put((byte) (n));
    }

    public static int readInt(ByteBuffer b) {
        return (b.get() & 255) << 24 | (b.get() & 255) << 16 | (b.get() & 255) << 8 | (b.get() & 255);
    }

    public static void writeLong(ByteBuffer b, long n) {
        b.put((byte) (n >> 56));
        b.put((byte) (n >> 48));
        b.put((byte) (n >> 40));
        b.put((byte) (n >> 32));
        b.put((byte) (n >> 24));
        b.put((byte) (n >> 16));
        b.put((byte) (n >> 8));
        b.put((byte) (n));
    }

    public static long readLong(ByteBuffer b) {
        return (b.get() & 255) << 56 | (b.get() & 255) << 48 | (b.get() & 255) << 40 | (b.get() & 255) << 32
                | (b.get() & 255) << 24 | (b.get() & 255) << 16 | (b.get() & 255) << 8 | (b.get() & 255);
    }

}
