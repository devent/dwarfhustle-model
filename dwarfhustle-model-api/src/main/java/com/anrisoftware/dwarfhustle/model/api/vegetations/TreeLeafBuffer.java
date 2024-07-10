package com.anrisoftware.dwarfhustle.model.api.vegetations;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

/**
 * Writes and reads {@link TreeLeaf} in a byte buffer.
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
public class TreeLeafBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = VegetationBuffer.SIZE;

    public static void setTreeLeaf(MutableDirectBuffer b, int off, TreeLeaf o) {
        VegetationBuffer.writeObject(b, off, o);
    }

    public static TreeLeaf getTreeLeaf(DirectBuffer b, int off, TreeLeaf o) {
        VegetationBuffer.readObject(b, off, o);
        return o;
    }

}
