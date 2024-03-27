package com.anrisoftware.dwarfhustle.model.api.objects;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Writes and reads {@link GameChunkPos} in a byte buffer.
 * <p>
 * <code>[xxxx][yyyy][zzzz][xxxx][yyyy][zzzz]</code>
 */
public class GameChunkPosBuffer extends GameBlockPosBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = 3 * 4 + GameBlockPosBuffer.SIZE;

    private static final int EX_INDEX = 3;

    private static final int EY_INDEX = 4;

    private static final int EZ_INDEX = 5;

    public static void setEx(ByteBuffer b, int offset, int x) {
        b.position(offset);
        var buffer = b.asIntBuffer();
        buffer.put(EX_INDEX, x);
    }

    public static int getEx(ByteBuffer b, int offset) {
        return b.position(offset).asIntBuffer().get(EX_INDEX);
    }

    public static void setEy(ByteBuffer b, int offset, int y) {
        b.position(offset);
        var buffer = b.asIntBuffer();
        buffer.put(EY_INDEX, y);
    }

    public static int getEy(ByteBuffer b, int offset) {
        return b.position(offset).asIntBuffer().get(EY_INDEX);
    }

    public static void setEz(ByteBuffer b, int offset, int z) {
        b.position(offset);
        var buffer = b.asIntBuffer();
        buffer.put(EZ_INDEX, z);
    }

    public static int getEz(ByteBuffer b, int offset) {
        return b.position(offset).asIntBuffer().get(EZ_INDEX);
    }

    public static void writeGameChunkPos(ByteBuffer b, int offset, GameChunkPos p) {
        b.position(offset);
        var bi = b.asIntBuffer();
        putGameChunkPos(bi, p);
    }

    public static void putGameChunkPos(IntBuffer bi, GameChunkPos p) {
        bi.put(p.x);
        bi.put(p.y);
        bi.put(p.z);
        bi.put(p.ep.x);
        bi.put(p.ep.y);
        bi.put(p.ep.z);
    }

    public static GameChunkPos readGameChunkPos(ByteBuffer b, int offset) {
        b.position(offset);
        var bi = b.asIntBuffer();
        return getGameChunkPos(bi);
    }

    public static GameChunkPos getGameChunkPos(IntBuffer bi) {
        return new GameChunkPos(bi.get(), bi.get(), bi.get(), bi.get(), bi.get(), bi.get());
    }

}
