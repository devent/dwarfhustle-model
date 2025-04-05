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

import com.anrisoftware.dwarfhustle.model.api.buildings.Building;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.google.auto.service.AutoService;

/**
 * Writes and reads {@link Building} in a byte buffer.
 * <p>
 * See properties from {@link GameMapObjectBuffer}.
 * <ul>
 * <li>
 * </ul>
 *
 * <pre>
 * long  0                   1                   2                   3                   4                   5                   6
 * int   0         1         2         3         4         5         6         7         8         9         10        11        12
 * short 0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22   23   24   25
 *       iiii iiii iiii iiii kkkk kkkk oooo oooo mmmm mmmm mmmm mmmm xxxx yyyy zzzz tttt wwww hhhh dddd llll pppp pppp nnnn nnnn nnnn nnnn
 * </pre>
 */
@AutoService(StoredObjectBuffer.class)
public class BuildingBuffer implements StoredObjectBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = GameMapObjectBuffer.SIZE //
            + 8 // name
    ;

    private static final int NAME_BYTES = 22 * 2;

    public static void setName(MutableDirectBuffer b, int off, long name) {
        b.putLong(NAME_BYTES + off, name);
    }

    public static long getName(DirectBuffer b, int off) {
        return b.getLong(NAME_BYTES + off);
    }

    public static void setBuilding(MutableDirectBuffer b, int off, Building o) {
        GameMapObjectBuffer.writeObject(b, off, o);
        setName(b, off, o.getName());
    }

    public static Building getBuilding(DirectBuffer b, int off, Building o) {
        GameMapObjectBuffer.readObject(b, off, o);
        o.setName(getName(b, off));
        return o;
    }

    @Override
    public StoredObject read(DirectBuffer b) {
        return getBuilding(b, 0, new Building());
    }

    @Override
    public int getObjectType() {
        return Building.OBJECT_TYPE;
    }

    @Override
    public int getSize(StoredObject go) {
        return SIZE;
    }

    @Override
    public void write(MutableDirectBuffer b, StoredObject go) {
        setBuilding(b, 0, (Building) go);
    }
}
