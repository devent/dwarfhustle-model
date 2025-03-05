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

import com.anrisoftware.dwarfhustle.model.api.vegetations.Vegetation;

/**
 * Writes and reads {@link Vegetation} in a byte buffer.
 * <p>
 * See properties from {@link GameMapObjectBuffer}.
 * <ul>
 * <li>@{code g} the growth;
 * <li>@{code G} the growth step;
 * </ul>
 *
 * <pre>
 * long  0                   1                   2                   3                   4                   5
 * int   0         1         2         3         4         5         6         7         8         9         10
 * short 0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21
 *       iiii iiii iiii iiii kkkk kkkk oooo oooo mmmm mmmm mmmm mmmm xxxx yyyy zzzz pppp pppp tttt llll gggg gggg GGGG
 * </pre>
 */
public class VegetationBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = GameMapObjectBuffer.SIZE //
            + 4 // g
            + 2 // G
    ;

    private static final int GROWTH_BYTE = 19 * 2;

    private static final int GROWTH_STEP_BYTE = 21 * 2;

    public static void setGrowth(MutableDirectBuffer b, int off, float g) {
        b.putFloat(GROWTH_BYTE + off, g);
    }

    public static float getGrowth(DirectBuffer b, int off) {
        return b.getFloat(GROWTH_BYTE + off);
    }

    public static void setGrowthStep(MutableDirectBuffer b, int off, int g) {
        b.putShort(GROWTH_STEP_BYTE + off, (short) g);
    }

    public static int getGrowthStep(DirectBuffer b, int off) {
        return b.getShort(GROWTH_STEP_BYTE + off);
    }

    public static void writeVegetation(MutableDirectBuffer b, int off, Vegetation o) {
        GameMapObjectBuffer.writeObject(b, off, o);
        setGrowth(b, off, o.growth);
        setGrowthStep(b, off, o.growthStep);
    }

    public static Vegetation readVegetation(DirectBuffer b, int off, Vegetation o) {
        GameMapObjectBuffer.readObject(b, off, o);
        o.growth = getGrowth(b, off);
        o.growthStep = getGrowthStep(b, off);
        return o;
    }

}
