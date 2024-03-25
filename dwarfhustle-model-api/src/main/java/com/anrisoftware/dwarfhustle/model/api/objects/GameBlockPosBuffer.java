package com.anrisoftware.dwarfhustle.model.api.objects;

import java.nio.ByteBuffer;

/**
 * Writes and reads {@link GameBlockPos} in a byte buffer.
 * <p>
 * <code>[xxxx][yyyy][zzzz]</code>
 */
public class GameBlockPosBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = 3 * 4;

    private static final int X_INDEX = 0;

    private static final int Y_INDEX = 1;

    private static final int Z_INDEX = 2;

    public static void setX(ByteBuffer b, int offset, int x) {
        b.position(offset);
        var buffer = b.asIntBuffer();
        buffer.put(X_INDEX, x);
    }

    public static int getX(ByteBuffer b, int offset) {
        return b.position(offset).asIntBuffer().get(X_INDEX);
    }

    public static void setY(ByteBuffer b, int offset, int y) {
        b.position(offset);
        var buffer = b.asIntBuffer();
        buffer.put(Y_INDEX, y);
    }

    public static int getY(ByteBuffer b, int offset) {
        return b.position(offset).asIntBuffer().get(Y_INDEX);
    }

    public static void setZ(ByteBuffer b, int offset, int z) {
        b.position(offset);
        var buffer = b.asIntBuffer();
        buffer.put(Z_INDEX, z);
    }

    public static int getZ(ByteBuffer b, int offset) {
        return b.position(offset).asIntBuffer().get(Z_INDEX);
    }

}
