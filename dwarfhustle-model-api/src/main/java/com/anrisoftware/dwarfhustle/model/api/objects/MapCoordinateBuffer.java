/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
 * Copyright © 2023 Erwin Müller (erwin.mueller@anrisoftware.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
