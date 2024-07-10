package com.anrisoftware.dwarfhustle.model.api.vegetations;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

/**
 * Writes and reads {@link TreeTwig} in a byte buffer.
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
public class TreeTwigBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = VegetationBuffer.SIZE;

    public static void setTreeTwig(MutableDirectBuffer b, int off, TreeTwig o) {
        VegetationBuffer.writeObject(b, off, o);
    }

    public static TreeTwig getTreeTwig(DirectBuffer b, int off, TreeTwig o) {
        VegetationBuffer.readObject(b, off, o);
        return o;
    }

}
