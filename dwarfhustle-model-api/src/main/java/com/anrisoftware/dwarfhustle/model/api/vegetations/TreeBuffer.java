package com.anrisoftware.dwarfhustle.model.api.vegetations;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

/**
 * Writes and reads {@link KnowledgeVegetation} in a byte buffer.
 * 
 * <ul>
 * <li>@{code i} the KID;
 * <li>@{code g} the growth;
 * </ul>
 * 
 * <pre>
 * int   0         1         2         3
 * short 0    1    2    3    4    5    6
 *       iiii iiii gggg
 * </pre>
 */
public class TreeBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = 4;

    private static final int KID_INDEX_BYTES = 0 * 4;

    private static final int GROWTH_INDEX_BYTES = 1 * 4;

    public static void setKid(int off, MutableDirectBuffer b, int id) {
        b.putInt(KID_INDEX_BYTES + off, id);
    }

    public static int getKid(int off, DirectBuffer b) {
        return b.getInt(KID_INDEX_BYTES + off);
    }

    public static void setGrowth(int off, MutableDirectBuffer b, float g) {
        b.putShort(GROWTH_INDEX_BYTES + off, (short) (g * 65_536f - 32_768f));
    }

    public static float getLux(int off, DirectBuffer b) {
        return b.getShort(GROWTH_INDEX_BYTES + off) / 65_536f + 32_768f;
    }

}
