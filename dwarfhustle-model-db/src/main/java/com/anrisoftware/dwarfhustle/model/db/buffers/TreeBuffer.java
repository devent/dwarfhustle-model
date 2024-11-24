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
package com.anrisoftware.dwarfhustle.model.db.buffers;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.anrisoftware.dwarfhustle.model.api.vegetations.Tree;
import com.google.auto.service.AutoService;

/**
 * Writes and reads {@link Tree} in a byte buffer.
 * 
 * <ul>
 * <li>@{code i} the object ID;
 * <li>@{code k} the knowledge ID;
 * <li>@{code m} the map ID;
 * <li>@{code x} the X position on the map;
 * <li>@{code y} the Y position on the map;
 * <li>@{code z} the Z position on the map;
 * <li>@{code g} the growth;
 * </ul>
 * 
 * <pre>
 * long  0                   1                   2                   3
 * int   0         1         2         3         4         5         6         7
 * short 0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15
 *       iiii iiii iiii iiii kkkk kkkk kkkk kkkk mmmm mmmm mmmm mmmm xxxx yyyy zzzz gggg
 * </pre>
 */
@AutoService(StoredObjectBuffer.class)
public class TreeBuffer implements StoredObjectBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = VegetationBuffer.SIZE;

    public static void setTree(MutableDirectBuffer b, int off, Tree o) {
        VegetationBuffer.writeVegetation(b, off, o);
    }

    public static Tree getTree(DirectBuffer b, int off, Tree o) {
        VegetationBuffer.readVegetation(b, off, o);
        return o;
    }

    @Override
    public int getObjectType() {
        return Tree.OBJECT_TYPE;
    }

    @Override
    public int getSize(StoredObject go) {
        return SIZE;
    }

    @Override
    public StoredObject read(DirectBuffer b) {
        return getTree(b, 0, new Tree());
    }

    @Override
    public void write(MutableDirectBuffer b, StoredObject go) {
        setTree(b, 0, (Tree) go);
    }
}
