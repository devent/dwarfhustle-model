package com.anrisoftware.dwarfhustle.model.api.objects;

import java.time.ZoneOffset;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

/**
 * Writes and reads {@link ZoneOffset} in a byte buffer.
 * 
 * 
 * <ul>
 * <li>@{code s} the total seconds;
 * </ul>
 * 
 * <pre>
 * long  0
 * int   0         1         2         3
 * short 0    1    2    3    4    5    6    7
 *       ssss ssss
 * </pre>
 */
public class ZoneOffsetBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = //
            2 * 4;

    public static void setZoneOffset(MutableDirectBuffer b, int off, ZoneOffset zone) {
        setZoneOffset(b, off, zone.getTotalSeconds());
    }

    public static void setZoneOffset(MutableDirectBuffer b, int off, int seconds) {
        b.putInt(off, seconds);
    }

    public static ZoneOffset getZoneOffset(DirectBuffer b, int off) {
        return ZoneOffset.ofTotalSeconds(b.getInt(off));
    }

}
