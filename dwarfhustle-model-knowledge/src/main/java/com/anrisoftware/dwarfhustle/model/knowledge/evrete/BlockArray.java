package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Sizes:
 * 
 * <pre>
 * 512 * 512 * 128 * ( 
 * + 2 parent
 * + 2 material
 * + 2 object
 * + 4 p
 * + 2 temp
 * + 2 lux
 * )
 * = 448 MB
 * </pre>
 */
public class BlockArray {

    /**
     * Writes and reads the properties of a block in a byte buffer.
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
     * int   0         1         2         3
     * short 0    1    2    3    4    5    6
     *       pppp pppp PPPP mmmm oooo tttt llll
     * </pre>
     */
    public static class Block {

        public static int SIZE = 2 + 2 + 2 + 4 + 2 + 2;

        private static final int PARENT_SHORT_INDEX = 2;

        private static final int MATERIAL_SHORT_INDEX = 3;

        private static final int OBJECT_SHORT_INDEX = 4;

        private static final int PROP_INT_INDEX = 0;

        private static final int TEMP_SHORT_INDEX = 5;

        private static final int LUX_SHORT_INDEX = 6;

        public static void setParent(ShortBuffer b, int off, short p) {
            b.put(PARENT_SHORT_INDEX + off, p);
        }

        public static short getParent(ShortBuffer b, int off) {
            return b.get(PARENT_SHORT_INDEX + off);
        }

        public static void setMaterial(ShortBuffer b, int off, short m) {
            b.put(MATERIAL_SHORT_INDEX + off, m);
        }

        public static short getMaterial(ShortBuffer b, int off) {
            return b.get(MATERIAL_SHORT_INDEX + off);
        }

        public static void setObject(ShortBuffer b, int off, short o) {
            b.put(OBJECT_SHORT_INDEX + off, o);
        }

        public static short getObject(ShortBuffer b, int off) {
            return b.get(OBJECT_SHORT_INDEX + off);
        }

        public static void setProp(IntBuffer b, int off, int p) {
            b.put(PROP_INT_INDEX + off, p);
        }

        public static int getProp(IntBuffer b, int off) {
            return b.get(PROP_INT_INDEX + off);
        }

        public static void setTemp(ShortBuffer b, int off, short t) {
            b.put(TEMP_SHORT_INDEX + off, t);
        }

        public static short getTemp(ShortBuffer b, int off) {
            return b.get(TEMP_SHORT_INDEX + off);
        }

        public static void setLux(ShortBuffer b, int off, short l) {
            b.put(LUX_SHORT_INDEX + off, l);
        }

        public static short getLux(ShortBuffer b, int off) {
            return b.get(LUX_SHORT_INDEX + off);
        }

    }

    public final ByteBuffer blocks;

    private final int w;

    private final int h;

    public final ShortBuffer shortBuffer;

    public final IntBuffer intBuffer;

    public BlockArray(int w, int h, int d) {
        this.w = w;
        this.h = h;
        this.blocks = ByteBuffer.allocateDirect(w * h * d * Block.SIZE);
        this.shortBuffer = blocks.asShortBuffer();
        this.intBuffer = blocks.asIntBuffer();
    }

    public void setMaterial(int x, int y, int z, short m) {
        Block.setMaterial(shortBuffer, getShortIndex(x, y, z), m);
    }

    public short getMaterial(int x, int y, int z) {
        return Block.getMaterial(shortBuffer, getShortIndex(x, y, z));
    }

    public void setProp(int x, int y, int z, int p) {
        Block.setProp(intBuffer, getIntIndex(x, y, z), p);
    }

    public int getProp(int x, int y, int z) {
        return Block.getProp(intBuffer, getIntIndex(x, y, z));
    }

    public boolean isProp(int x, int y, int z, int flags) {
        return (Block.getProp(intBuffer, getIntIndex(x, y, z)) & flags) == flags;
    }

    public void setTemp(int x, int y, int z, short t) {
        Block.setTemp(shortBuffer, getShortIndex(x, y, z), t);
    }

    public short getTemp(int x, int y, int z) {
        return Block.getTemp(shortBuffer, getShortIndex(x, y, z));
    }

    public void setLux(int x, int y, int z, int l) {
        Block.setLux(shortBuffer, getShortIndex(x, y, z), (short) (l - 32_769));
    }

    public int getLux(int x, int y, int z) {
        return Block.getLux(shortBuffer, getShortIndex(x, y, z)) + 32_769;
    }

    public int getPos(int x, int y, int z) {
        return getIndex(x, y, z) * Block.SIZE;
    }

    public int getShortIndex(int x, int y, int z) {
        return getIndex(x, y, z) * (Block.SIZE / 2);
    }

    public int getIntIndex(int x, int y, int z) {
        return getIndex(x, y, z) * (Block.SIZE / 4);
    }

    private int getIndex(int x, int y, int z) {
        return z * w * h + y * w + x;
    }
}
