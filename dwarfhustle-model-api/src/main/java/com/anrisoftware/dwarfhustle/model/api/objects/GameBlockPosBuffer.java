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

    protected static final int X_INDEX = 0;

    protected static final int Y_INDEX = 1;

    protected static final int Z_INDEX = 2;

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

    public static void writeGameBlockPos(ByteBuffer b, int offset, GameBlockPos p) {
        b.position(offset);
        var bi = b.asIntBuffer();
        bi.put(X_INDEX, p.x);
        bi.put(Y_INDEX, p.y);
        bi.put(Z_INDEX, p.z);
    }

    public static GameBlockPos readGameBlockPos(ByteBuffer b, int offset) {
        b.position(offset);
        var bi = b.asIntBuffer();
        return new GameBlockPos(bi.get(X_INDEX), bi.get(Y_INDEX), bi.get(Z_INDEX));
    }

}
