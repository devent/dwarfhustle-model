package com.anrisoftware.dwarfhustle.model.api.objects;

import java.nio.ByteBuffer;

/**
 * Writes and reads CID := {@link GameChunkPos} entries in a buffer.
 * 
 * <pre>
 * 0      1    2    3    4    5    6    7    8
 * 0           1         2         3         4
 * [CCCC]([iiii][xxxxyyyyzzzzXXXXYYYYZZZZ])...]
 * </pre>
 */
public class CidGameChunkPosMapBuffer {

    /**
     * Size in bytes without chunks.
     */
    public static final int SIZE_MIN = 4;

    /**
     * Size of one entry.
     */
    public static final int SIZE_ENTRY = 4 + GameChunkPosBuffer.SIZE;

    private static final int COUNT_INDEX = 0;

    private static final int ID_OFFSET = 4;

    private static final int POS_OFFSET = 4;

    public static void setCount(ByteBuffer b, int offset, int c) {
        b.position(offset);
        var buffer = b.asIntBuffer();
        buffer.put(COUNT_INDEX, c);
    }

    public static int getCount(ByteBuffer b, int offset) {
        return b.position(offset).asIntBuffer().get(COUNT_INDEX);
    }

    public static void setEntry(ByteBuffer b, int offset, int i, int[] entry) {
        assert entry.length == 7;
        setEntry(b, offset, i, entry[0], entry[1], entry[2], entry[3], entry[4], entry[5], entry[6]);
    }

    public static void setEntry(ByteBuffer b, int offset, int i, int cid, int sx, int sy, int sz, int ex, int ey,
            int ez) {
        offset += ID_OFFSET + i * SIZE_ENTRY;
        b.position(offset);
        var buffer = b.asIntBuffer();
        buffer.put(cid);
        offset += POS_OFFSET;
        GameChunkPosBuffer.setX(b, offset, sx);
        GameChunkPosBuffer.setY(b, offset, sy);
        GameChunkPosBuffer.setZ(b, offset, sz);
        GameChunkPosBuffer.setEx(b, offset, ex);
        GameChunkPosBuffer.setEy(b, offset, ey);
        GameChunkPosBuffer.setEz(b, offset, ez);
    }

    public static void setEntries(ByteBuffer b, int offset, int count, int[] entries) {
        b.position(offset);
        var buffer = b.asIntBuffer();
        buffer.put(count);
        for (int i = 0; i < count; i++) {
            int o = offset + ID_OFFSET + i * SIZE_ENTRY;
            b.position(o);
            buffer = b.asIntBuffer();
            buffer.put(entries[i * 7 + 0]);
            o += POS_OFFSET;
            GameChunkPosBuffer.setX(b, o, entries[i * 7 + 1]);
            GameChunkPosBuffer.setY(b, o, entries[i * 7 + 2]);
            GameChunkPosBuffer.setZ(b, o, entries[i * 7 + 3]);
            GameChunkPosBuffer.setEx(b, o, entries[i * 7 + 4]);
            GameChunkPosBuffer.setEy(b, o, entries[i * 7 + 5]);
            GameChunkPosBuffer.setEz(b, o, entries[i * 7 + 6]);
        }
    }

    public static int[] getEntries(ByteBuffer b, int offset, int[] dest) {
        b.position(offset);
        var buffer = b.asIntBuffer();
        int count = buffer.get();
        if (dest == null) {
            dest = new int[count * 7];
        }
        for (int i = 0; i < count; i++) {
            int o = offset + ID_OFFSET + i * SIZE_ENTRY;
            b.position(o);
            buffer = b.asIntBuffer();
            dest[i * 7 + 0] = buffer.get();
            o += POS_OFFSET;
            dest[i * 7 + 1] = GameChunkPosBuffer.getX(b, o);
            dest[i * 7 + 2] = GameChunkPosBuffer.getY(b, o);
            dest[i * 7 + 3] = GameChunkPosBuffer.getZ(b, o);
            dest[i * 7 + 4] = GameChunkPosBuffer.getEx(b, o);
            dest[i * 7 + 5] = GameChunkPosBuffer.getEy(b, o);
            dest[i * 7 + 6] = GameChunkPosBuffer.getEz(b, o);
        }
        return dest;
    }

    public static int getCid(ByteBuffer b, int offset, int i) {
        offset += ID_OFFSET + i * SIZE_ENTRY;
        b.position(offset);
        var buffer = b.asIntBuffer();
        return buffer.get();
    }

    public static int getSx(ByteBuffer b, int offset, int i) {
        offset += POS_OFFSET + ID_OFFSET + i * SIZE_ENTRY;
        return GameChunkPosBuffer.getX(b, offset);
    }

    public static int getSy(ByteBuffer b, int offset, int i) {
        offset += POS_OFFSET + ID_OFFSET + i * SIZE_ENTRY;
        return GameChunkPosBuffer.getY(b, offset);
    }

    public static int getSz(ByteBuffer b, int offset, int i) {
        offset += POS_OFFSET + ID_OFFSET + i * SIZE_ENTRY;
        return GameChunkPosBuffer.getZ(b, offset);
    }

    public static int getEx(ByteBuffer b, int offset, int i) {
        offset += POS_OFFSET + ID_OFFSET + i * SIZE_ENTRY;
        return GameChunkPosBuffer.getEx(b, offset);
    }

    public static int getEy(ByteBuffer b, int offset, int i) {
        offset += POS_OFFSET + ID_OFFSET + i * SIZE_ENTRY;
        return GameChunkPosBuffer.getEy(b, offset);
    }

    public static int getEz(ByteBuffer b, int offset, int i) {
        offset += POS_OFFSET + ID_OFFSET + i * SIZE_ENTRY;
        return GameChunkPosBuffer.getEz(b, offset);
    }

}
