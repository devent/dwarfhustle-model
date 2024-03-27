package com.anrisoftware.dwarfhustle.model.api.objects;

import java.nio.ByteBuffer;

/**
 * Writes and reads {@link MapChunk} in a byte buffer.
 * 
 * <ul>
 * <li>@{code c} index entries count;
 * <li>@{code i} chunk CID;
 * <li>@{code p} chunk position;
 * <li>@{code s} chunk size;
 * </ul>
 * 
 * <pre>
 * long 0         1
 * int  0    1    2    3
 * byte 0    4    8    12
 *      cccc iiii pppp ssss ....
 * </pre>
 */
public class MapChunksIndexBuffer {

    /**
     * Size in bytes without chunk entries.
     */
    public static final int SIZE_MIN = 4;

    /**
     * Size of one entry.
     */
    public static final int SIZE_ENTRY = 3 * 4;

    private static final int COUNT_INT_INDEX = 0;

    private static final int ID_OFFSET = 4;

    private static final int POS_OFFSET = 8;

    private static final int SIZE_OFFSET = 12;

    public static void setCount(ByteBuffer b, int offset, int c) {
        b.position(offset);
        var bi = b.asIntBuffer();
        bi.put(COUNT_INT_INDEX, c);
    }

    public static int getCount(ByteBuffer b, int offset) {
        return b.position(offset).asIntBuffer().get(COUNT_INT_INDEX);
    }

    public static void setEntry(ByteBuffer b, int offset, int i, int[] entry) {
        assert entry.length == 3;
        setEntry(b, offset, i, entry[0], entry[1], entry[2]);
    }

    public static void setEntry(ByteBuffer b, int offset, int i, int cid, int pos, int size) {
        offset += ID_OFFSET + i * SIZE_ENTRY;
        b.position(offset);
        var bi = b.asIntBuffer();
        bi.put(cid);
        bi.put(pos);
        bi.put(size);
    }

    public static void setEntries(ByteBuffer b, int offset, int count, int[] entries) {
        b.position(offset);
        var bi = b.asIntBuffer();
        bi.put(count);
        for (int i = 0; i < count; i++) {
            bi.put(entries[i * 3 + 0]);
            bi.put(entries[i * 3 + 1]);
            bi.put(entries[i * 3 + 2]);
        }
    }

    public static int[] getEntries(ByteBuffer b, int offset, int[] dest) {
        b.position(offset);
        var bi = b.asIntBuffer();
        int count = bi.get();
        if (dest == null) {
            dest = new int[count * 3];
        }
        for (int i = 0; i < count; i++) {
            dest[i * 3 + 0] = bi.get();
            dest[i * 3 + 1] = bi.get();
            dest[i * 3 + 2] = bi.get();
        }
        return dest;
    }

    public static int getCid(ByteBuffer b, int offset, int i) {
        offset += ID_OFFSET + i * SIZE_ENTRY;
        b.position(offset);
        var buffer = b.asIntBuffer();
        return buffer.get();
    }

    public static int getPos(ByteBuffer b, int offset, int i) {
        offset += POS_OFFSET + i * SIZE_ENTRY;
        b.position(offset);
        var buffer = b.asIntBuffer();
        return buffer.get();
    }

    public static int getSize(ByteBuffer b, int offset, int i) {
        offset += SIZE_OFFSET + i * SIZE_ENTRY;
        b.position(offset);
        var buffer = b.asIntBuffer();
        return buffer.get();
    }

}
