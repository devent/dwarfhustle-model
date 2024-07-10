package com.anrisoftware.dwarfhustle.model.api.vegetations;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

/**
 * Writes and reads {@link Grass} in a byte buffer.
 * 
 * <ul>
 * <li>@{code i} the ID;
 * </ul>
 * 
 * <pre>
 * int   0         1         2         3
 * short 0    1    2    3    4    5    6
 *       iiii iiii iiii iiii
 * </pre>
 */
public class GrassBuffer extends VegetationBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = VegetationBuffer.SIZE;

    public static void setGrass(MutableDirectBuffer b, int off, Grass o) {
        VegetationBuffer.writeObject(b, off, o);
    }

    public static Grass getGrass(DirectBuffer b, int off, Grass o) {
        VegetationBuffer.readObject(b, off, o);
        return o;
    }

}