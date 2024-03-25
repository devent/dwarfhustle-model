package com.anrisoftware.dwarfhustle.model.api.objects;

import java.nio.ByteBuffer;

/**
 * Writes and reads {@link MapBlock} in a byte buffer.
 * 
 * <ul>
 * <li>@{code m} material ID;
 * <li>@{code o} object ID;
 * <li>@{code P} parent chunk CID;
 * <li>@{code i} map block position index;
 * <li>@{code p} tile properties;
 * </ul>
 * 
 * <pre>
 * long 0         1         2         3
 * int  0    1    2    3    4    5    6
 * byte 0    4    8    12   16   20   24
 *      mmmm mmmm oooo oooo PPPP iiii pppp
 * </pre>
 */
public class MapBlockBuffer extends GameBlockPosBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = 8 + 8 + 4 + 4 + 4;

    private static final int INDEX_INT_INDEX = 5;

    private static final int PARENT_INT_INDEX = 4;

    private static final int MAT_LONG_INDEX = 0;

    private static final int OBJECT_LONG_INDEX = 1;

    private static final int PROP_INT_INDEX = 6;

    /**
     * Returns the X position from the index.
     */
    public static int calcX(int i, int w) {
        return i % w;
    }

    /**
     * Returns the Y position from the index.
     */
    public static int calcY(int i, int w) {
        return Math.floorMod(i / w, w);
    }

    /**
     * Returns the Z position from the index.
     */
    public static int calcZ(int i, int w, int h) {
        return (int) Math.floor(i / w / h);
    }

    public static void setIndex(ByteBuffer b, int offset, int i) {
        b.position(offset);
        var buffer = b.asIntBuffer();
        buffer.put(INDEX_INT_INDEX, i);
    }

    public static int getIndex(ByteBuffer b, int offset) {
        return b.asIntBuffer().get(INDEX_INT_INDEX);
    }

    public static void setProp(ByteBuffer b, int offset, int p) {
        b.position(offset);
        var buffer = b.asIntBuffer();
        buffer.put(PROP_INT_INDEX, p);
    }

    public static int getProp(ByteBuffer b, int offset) {
        return b.asIntBuffer().get(PROP_INT_INDEX);
    }

    public static void setParent(ByteBuffer b, int offset, int p) {
        b.position(offset);
        var buffer = b.asIntBuffer();
        buffer.put(PARENT_INT_INDEX, p);
    }

    public static int getParent(ByteBuffer b, int offset) {
        return b.asIntBuffer().get(PARENT_INT_INDEX);
    }

    public static void setMaterial(ByteBuffer b, int offset, long m) {
        b.position(offset);
        var buffer = b.asLongBuffer();
        buffer.put(MAT_LONG_INDEX, m);
    }

    public static long getMaterial(ByteBuffer b, int offset) {
        return b.asLongBuffer().get(MAT_LONG_INDEX);
    }

    public static void setObject(ByteBuffer b, int offset, long o) {
        b.position(offset);
        var buffer = b.asLongBuffer();
        buffer.put(OBJECT_LONG_INDEX, o);
    }

    public static long getObject(ByteBuffer b, int offset) {
        return b.asLongBuffer().get(OBJECT_LONG_INDEX);
    }

}
