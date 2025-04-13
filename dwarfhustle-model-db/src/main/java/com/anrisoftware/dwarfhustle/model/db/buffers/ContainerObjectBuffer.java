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

import com.anrisoftware.dwarfhustle.model.api.miscobjects.ContainerObject;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.anrisoftware.dwarfhustle.model.api.objects.TableObject;
import com.google.auto.service.AutoService;

/**
 * Writes and reads {@link ContainerObject} in a byte buffer.
 * <p>
 * See properties from {@link GameMapMaterialObjectBuffer}.
 * <ul>
 * <li>@{code T} {@link TableObject} ID;
 * </ul>
 *
 * <pre>
 * long  0                   1                   2                   3                   4                   5                   6                   7
 * int   0         1         2         3         4         5         6         7         8         9         10        11        12        13        14
 * short 0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22   23   24   25   26   27   28   29
 *       iiii iiii iiii iiii kkkk kkkk oooo oooo mmmm mmmm mmmm mmmm xxxx yyyy zzzz tttt wwww hhhh dddd llll pppp pppp MMMM MMMM MMMM MMMM TTTT TTTT TTTT TTTT
 * </pre>
 */
@AutoService(StoredObjectBuffer.class)
public class ContainerObjectBuffer implements StoredObjectBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = GameMapMaterialObjectBuffer.SIZE //
            + 8 // storage ID
    ;

    private static final int TABLE_BYTES = 13 * 4;

    public static void setTable(MutableDirectBuffer b, int off, long tid) {
        b.putLong(TABLE_BYTES + off, tid);
    }

    public static long getTable(DirectBuffer b, int off) {
        return b.getLong(TABLE_BYTES + off);
    }

    public static void setContainerObject(MutableDirectBuffer b, int off, ContainerObject o) {
        GameMapMaterialObjectBuffer.writeMaterialObject(b, off, o);
        setTable(b, off, o.getTable());
    }

    public static ContainerObject getContainerObject(DirectBuffer b, int off, ContainerObject o) {
        GameMapMaterialObjectBuffer.readMaterialObject(b, off, o);
        o.setTable(getTable(b, off));
        return o;
    }

    @Override
    public StoredObject read(DirectBuffer b) {
        return getContainerObject(b, 0, new ContainerObject());
    }

    @Override
    public int getObjectType() {
        return ContainerObject.OBJECT_TYPE;
    }

    @Override
    public int getSize(StoredObject go) {
        return SIZE;
    }

    @Override
    public void write(MutableDirectBuffer b, StoredObject go) {
        setContainerObject(b, 0, (ContainerObject) go);
    }
}
