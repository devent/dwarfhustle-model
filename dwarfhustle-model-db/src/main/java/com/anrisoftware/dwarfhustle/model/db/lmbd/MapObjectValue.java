package com.anrisoftware.dwarfhustle.model.db.lmbd;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

import lombok.RequiredArgsConstructor;

/**
 * Stores the object ID and object type for the game map object.
 * 
 * <ul>
 * <li>@{code i} the ID;
 * </ul>
 * 
 * <pre>
 * long  0
 * int   0         1         2         3
 * short 0    1    2    3    4    5    6
 *       iiii iiii iiii iiii tttt tttt
 * </pre>
 */
@RequiredArgsConstructor
public class MapObjectValue {

    /**
     * Size in bytes.
     */
    public static final int SIZE = 1 * 8 + 1 * 4;

    private static final int ID_INDEX = 0 * 8;

    private static final int TYPE_INDEX = 2 * 4;

    public static void setId(MutableDirectBuffer b, int off, long id) {
        b.putLong(ID_INDEX + off, id);
    }

    public static long getId(DirectBuffer b, int off) {
        return b.getLong(ID_INDEX + off);
    }

    public static void setType(MutableDirectBuffer b, int off, int type) {
        b.putInt(TYPE_INDEX + off, type);
    }

    public static int getType(DirectBuffer b, int off) {
        return b.getInt(TYPE_INDEX + off);
    }
}
