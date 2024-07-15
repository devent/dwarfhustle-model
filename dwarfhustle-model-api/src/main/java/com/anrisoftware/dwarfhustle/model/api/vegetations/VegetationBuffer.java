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
package com.anrisoftware.dwarfhustle.model.api.vegetations;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

import com.anrisoftware.dwarfhustle.model.api.objects.BufferUtils;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObjectBuffer;

/**
 * Writes and reads {@link Vegetation} in a byte buffer.
 * 
 * <ul>
 * <li>@{code i} the KID;
 * <li>@{code g} the growth;
 * </ul>
 * 
 * <pre>
 * long  0                   1                   2                   3
 * int   0         1         2         3         4         5         6
 * short 0    1    2    3    4    5    6    7    8    9    10   11   12   13
 *       iiii iiii iiii iiii mmmm mmmm mmmm mmmm xxxx yyyy zzzz kkkk kkkk gggg
 * </pre>
 */
public class VegetationBuffer extends GameMapObjectBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = GameMapObjectBuffer.SIZE + 4 + 2;

    private static final int KID_INDEX_BYTES = 11 * 2;

    private static final int GROWTH_INDEX_BYTES = 13 * 2;

    public static void setKid(MutableDirectBuffer b, int off, int id) {
        b.putInt(KID_INDEX_BYTES + off, id);
    }

    public static int getKid(DirectBuffer b, int off) {
        return b.getInt(KID_INDEX_BYTES + off);
    }

    public static void setGrowth(MutableDirectBuffer b, int off, float g) {
        b.putShort(GROWTH_INDEX_BYTES + off, BufferUtils.floatToShort(g));
    }

    public static float getGrowth(DirectBuffer b, int off) {
        return BufferUtils.shortToFloat(b.getShort(GROWTH_INDEX_BYTES + off));
    }

    public static void writeObject(MutableDirectBuffer b, int off, Vegetation o) {
        GameMapObjectBuffer.writeObject(b, off, o);
        setKid(b, off, o.kid);
        setGrowth(b, off, o.growth);
    }

    public static Vegetation readObject(DirectBuffer b, int off, Vegetation o) {
        GameMapObjectBuffer.readObject(b, off, o);
        o.kid = getKid(b, off);
        o.growth = getGrowth(b, off);
        return o;
    }

}
