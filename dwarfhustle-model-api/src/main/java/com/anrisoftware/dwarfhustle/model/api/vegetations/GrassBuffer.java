package com.anrisoftware.dwarfhustle.model.api.vegetations;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

/**
 * Writes and reads {@link GameObject} in a byte buffer.
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
public class GrassBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = 8;

    private static final int ID_INDEX = 0;

    public static void setId(long id, MutableDirectBuffer b) {
        b.putLong(ID_INDEX, id);
    }

    public static long getId(DirectBuffer b) {
        return b.getLong(ID_INDEX);
    }
}
