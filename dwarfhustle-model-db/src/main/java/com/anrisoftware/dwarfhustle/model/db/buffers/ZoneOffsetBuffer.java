/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.db.buffers;

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
