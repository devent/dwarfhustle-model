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

import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.anrisoftware.dwarfhustle.model.api.vegetations.TreeRoot;
import com.google.auto.service.AutoService;

/**
 * Writes and reads {@link TreeRoot} in a byte buffer.
 * <p>
 * See properties from {@link GameMapObjectBuffer}.
 * <ul>
 * <li>
 * </ul>
 * 
 * <pre>
 * long  0                   1                   2                   3                   4
 * int   0         1         2         3         4         5         6         7         8
 * short 0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16
 *       iiii iiii iiii iiii kkkk kkkk oooo oooo mmmm mmmm mmmm mmmm xxxx yyyy zzzz pppp pppp
 * </pre>
 */
@AutoService(StoredObjectBuffer.class)
public class TreeRootBuffer implements StoredObjectBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = GameMapObjectBuffer.SIZE;

    public static void setTreeRoot(MutableDirectBuffer b, int off, TreeRoot o) {
        GameMapObjectBuffer.writeObject(b, off, o);
    }

    public static TreeRoot getTreeRoot(DirectBuffer b, int off, TreeRoot o) {
        GameMapObjectBuffer.readObject(b, off, o);
        return o;
    }

    @Override
    public StoredObject read(DirectBuffer b) {
        return getTreeRoot(b, 0, new TreeRoot());
    }

    @Override
    public int getObjectType() {
        return TreeRoot.OBJECT_TYPE;
    }

    @Override
    public int getSize(StoredObject go) {
        return SIZE;
    }

    @Override
    public void write(MutableDirectBuffer b, StoredObject go) {
        setTreeRoot(b, 0, (TreeRoot) go);
    }

}
