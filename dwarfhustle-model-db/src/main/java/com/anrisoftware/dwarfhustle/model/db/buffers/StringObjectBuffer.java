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

import com.anrisoftware.dwarfhustle.model.api.objects.StringObject;

/**
 * Writes and reads {@link StringObject} in a byte buffer.
 *
 * <ul>
 * <li>@{code i} the ID;
 * <li>@{code l} the length of the string;
 * <li>@{code s} the string;
 * </ul>
 *
 * <pre>
 * long  0
 * int   0         1         2         3
 * short 0    1    2    3    4    5    6
 *       iiii iiii iiii iiii llll llll ssss ....
 * </pre>
 */
public class StringObjectBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE_MIN = GameObjectBuffer.SIZE + DirectBuffer.STR_HEADER_LEN;

    private static final int S_INDEX = 6 * 4;

    public static void setString(MutableDirectBuffer b, int off, String s) {
        b.putStringUtf8(S_INDEX + off, s);
    }

    public static String getString(DirectBuffer b, int off) {
        return b.getStringUtf8(S_INDEX + off);
    }

    public static void writeStringObject(MutableDirectBuffer b, int off, StringObject o) {
        GameObjectBuffer.writeObject(b, off, o);
        setString(b, off, o.getS());
    }

    public static StringObject readStringObject(DirectBuffer b, int off, StringObject o) {
        GameObjectBuffer.readObject(b, off, o);
        o.setS(getString(b, off));
        return o;
    }
}
