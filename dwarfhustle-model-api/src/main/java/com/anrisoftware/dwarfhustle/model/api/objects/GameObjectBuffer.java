package com.anrisoftware.dwarfhustle.model.api.objects;

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
 * long  0
 * int   0         1         2         3
 * short 0    1    2    3    4    5    6
 *       iiii iiii iiii iiii
 * </pre>
 */
public class GameObjectBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = 8;

    private static final int ID_INDEX = 0 * 8;

    public static void setId(MutableDirectBuffer b, int off, long id) {
        b.putLong(ID_INDEX + off, id);
    }

    public static long getId(DirectBuffer b, int off) {
        return b.getLong(ID_INDEX + off);
    }

    public static void writeObject(MutableDirectBuffer b, int off, GameObject o) {
        setId(b, off, o.id);
    }

    public static GameObject readObject(DirectBuffer b, int off, GameObject o) {
        o.id = getId(b, off);
        return o;
    }
}
