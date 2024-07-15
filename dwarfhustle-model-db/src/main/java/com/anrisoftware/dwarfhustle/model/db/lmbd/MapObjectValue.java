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
