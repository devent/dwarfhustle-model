package com.anrisoftware.dwarfhustle.model.api.vegetations;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

/**
 * Writes and reads {@link TreeTrunk} in a byte buffer.
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
public class TreeTrunkBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = VegetationBuffer.SIZE;

    public static void setTreeTrunk(MutableDirectBuffer b, int off, TreeTrunk o) {
        VegetationBuffer.writeObject(b, off, o);
    }

    public static TreeTrunk getTreeTrunk(DirectBuffer b, int off, TreeTrunk o) {
        VegetationBuffer.readObject(b, off, o);
        return o;
    }

}
