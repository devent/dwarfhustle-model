/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
 * Copyright © 2022-2024 Erwin Müller (erwin.mueller@anrisoftware.com)
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
import com.anrisoftware.dwarfhustle.model.api.vegetations.TreeSapling;
import com.google.auto.service.AutoService;

/**
 * Writes and reads {@link TreeSapling} in a byte buffer.
 * <p>
 * See properties from {@link VegetationBuffer}.
 * <ul>
 * <li>
 * </ul>
 * 
 * <pre>
 * long  0                   1                   2                   3                   4
 * int   0         1         2         3         4         5         6         7         8
 * short 0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17
 *       iiii iiii iiii iiii kkkk kkkk oooo oooo mmmm mmmm mmmm mmmm xxxx yyyy zzzz pppp pppp gggg
 * </pre>
 */
@AutoService(StoredObjectBuffer.class)
public class TreeSaplingBuffer implements StoredObjectBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = VegetationBuffer.SIZE;

    public static void setTreeSapling(MutableDirectBuffer b, int off, TreeSapling o) {
        VegetationBuffer.writeVegetation(b, off, o);
    }

    public static TreeSapling getTreeSapling(DirectBuffer b, int off, TreeSapling o) {
        VegetationBuffer.readVegetation(b, off, o);
        return o;
    }

    @Override
    public StoredObject read(DirectBuffer b) {
        return TreeSaplingBuffer.getTreeSapling(b, 0, new TreeSapling());
    }

    @Override
    public int getObjectType() {
        return TreeSapling.OBJECT_TYPE;
    }

    @Override
    public int getSize(StoredObject go) {
        return SIZE;
    }

    @Override
    public void write(MutableDirectBuffer b, StoredObject go) {
        setTreeSapling(b, 0, (TreeSapling) go);
    }

}
