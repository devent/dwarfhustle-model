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
import org.eclipse.collections.api.factory.primitive.LongSets;
import org.eclipse.collections.api.set.primitive.LongSet;
import org.eclipse.collections.api.set.primitive.MutableLongSet;

import com.anrisoftware.dwarfhustle.model.api.objects.TableObject;

/**
 * Writes and reads {@link TableObject} in a byte buffer.
 *
 * <ul>
 * <li>@{code i} the ID;
 * <li>@{code p} the parent object ID;
 * <li>@{code l} the size of the table;
 * <li>@{code c0} the child object ID;
 * </ul>
 *
 * <pre>
 * long  0                   1                   2                   3
 * int   0         1         2         3         4         5         6         7
 * short 0    1    2    3    4    5    6    7    8    9    10   11   12   13   14
 *       iiii iiii iiii iiii pppp pppp pppp pppp ssss ssss ccc0 ccc0 ccc0 ccc0 ...
 * </pre>
 */
public class TableObjectBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE_MIN = GameObjectBuffer.SIZE //
            + 8 // parent
            + 4 // size
    ;

    public static int calcSize(int size) {
        return SIZE_MIN + size * 8;
    }

    private static final int PARENT_INDEX = 4 * 2;

    private static final int SIZE_INDEX = 8 * 2;

    private static final int TABLE_INDEX = 10 * 2;

    public static void setId(MutableDirectBuffer b, int off, long id) {
        GameObjectBuffer.setId(b, off, id);
    }

    public static long getId(DirectBuffer b, int off) {
        return GameObjectBuffer.getId(b, off);
    }

    public static void setParent(MutableDirectBuffer b, int off, long parent) {
        b.putLong(PARENT_INDEX + off, parent);
    }

    public static long getParent(DirectBuffer b, int off) {
        return b.getLong(PARENT_INDEX + off);
    }

    public static void setSize(MutableDirectBuffer b, int off, int size) {
        b.putInt(SIZE_INDEX + off, size);
    }

    public static int getSize(DirectBuffer b, int off) {
        return b.getInt(SIZE_INDEX + off);
    }

    public static void setTable(MutableDirectBuffer b, int off, LongSet table) {
        setSize(b, off, table.size());
        int i = 0;
        for (final var it = table.longIterator(); it.hasNext();) {
            final long v = it.next();
            b.putLong(off + TABLE_INDEX + i * 8, v);
            i++;
        }
    }

    public static MutableLongSet getTable(DirectBuffer b, int off) {
        int size = getSize(b, off);
        MutableLongSet table = LongSets.mutable.withInitialCapacity(size);
        for (int i = 0; i < size; i++) {
            long v = b.getLong(off + TABLE_INDEX + i * 8);
            table.add(v);
        }
        return table;
    }

    public static void writeTableObject(MutableDirectBuffer b, int off, TableObject o) {
        GameObjectBuffer.writeObject(b, off, o);
        setParent(b, off, o.getParent());
        setTable(b, off, o.getTable());
    }

    public static TableObject readTableObject(DirectBuffer b, int off, TableObject o) {
        GameObjectBuffer.readObject(b, off, o);
        o.setParent(getParent(b, off));
        o.setTable(getTable(b, off));
        return o;
    }
}
