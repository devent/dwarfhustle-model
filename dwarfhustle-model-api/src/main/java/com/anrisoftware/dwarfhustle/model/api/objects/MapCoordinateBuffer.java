package com.anrisoftware.dwarfhustle.model.api.objects;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

/**
 * Writes and reads {@link MapCoordinate} in a byte buffer.
 * 
 * 
 * <ul>
 * <li>@{code a} the {@link MapCoordinate#lat};
 * <li>@{code o} the {@link MapCoordinate#lon};
 * </ul>
 * 
 * <pre>
 * long  0
 * int   0         1
 * short 0    1    2    3
 *       aaaa aaaa oooo oooo
 * </pre>
 */
public class MapCoordinateBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = //
            2 * 4;

    private static final int LAT_BYTES = 0 * 4;

    private static final int LON_BYTES = 1 * 4;

    public static void setLat(MutableDirectBuffer b, int off, float lat) {
        b.putFloat(LAT_BYTES + off, lat);
    }

    public static float getLat(DirectBuffer b, int off) {
        return b.getFloat(LAT_BYTES + off);
    }

    public static void setLon(MutableDirectBuffer b, int off, float lon) {
        b.putFloat(LON_BYTES + off, lon);
    }

    public static float getLon(DirectBuffer b, int off) {
        return b.getFloat(LON_BYTES + off);
    }

}
