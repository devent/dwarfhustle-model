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

import com.anrisoftware.dwarfhustle.model.api.objects.GameMapMaterialObject;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;

/**
 * Writes and reads {@link GameMapMaterialObject} in a byte buffer.
 *
 * <ul>
 * <li>@{code i} the object ID;
 * <li>@{code k} the knowledge ID;
 * <li>@{code o} the knowledge object ID;
 * <li>@{code m} the map ID;
 * <li>@{code x} the X position on the map;
 * <li>@{code y} the Y position on the map;
 * <li>@{code z} the Z position on the map;
 * <li>@{code w} the width;
 * <li>@{code h} the height;
 * <li>@{code d} the depth;
 * <li>@{code p} the object properties;
 * <li>@{code t} temperature;
 * <li>@{code t} light lux;
 * <li>@{code M} light material;
 * </ul>
 *
 * <pre>
 * long  0                   1                   2                   3                   4                   5                   6
 * int   0         1         2         3         4         5         6         7         8         9         10        11        12
 * short 0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22   23   24   25
 *       iiii iiii iiii iiii kkkk kkkk oooo oooo mmmm mmmm mmmm mmmm xxxx yyyy zzzz tttt wwww hhhh dddd llll pppp pppp MMMM MMMM MMMM MMMM
 * </pre>
 */
public class GameMapMaterialObjectBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = GameMapObjectBuffer.SIZE // 44
            + 8 // M
    ;

    private static final int MATERIAL_BYTES = 11 * 4;

    public static void setMaterial(MutableDirectBuffer b, int off, long id) {
        b.putLong(MATERIAL_BYTES + off, id);
    }

    public static long getMaterial(DirectBuffer b, int off) {
        return b.getLong(MATERIAL_BYTES + off);
    }

    public static void writeMaterialObject(MutableDirectBuffer b, int off, GameMapMaterialObject o) {
        GameMapObjectBuffer.writeObject(b, off, o);
        setMaterial(b, off, o.getMaterial());
    }

    public static GameMapObject readMaterialObject(DirectBuffer b, int off, GameMapMaterialObject o) {
        GameMapObjectBuffer.readObject(b, off, o);
        o.setMaterial(getMaterial(b, off));
        return o;
    }
}
