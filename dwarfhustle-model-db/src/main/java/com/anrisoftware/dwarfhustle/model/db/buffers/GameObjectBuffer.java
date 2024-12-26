/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
 * Copyright © 2022-2025 Erwin Müller (erwin.mueller@anrisoftware.com)
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

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;

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
