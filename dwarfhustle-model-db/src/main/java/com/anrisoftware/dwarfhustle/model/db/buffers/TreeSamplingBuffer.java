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
import com.anrisoftware.dwarfhustle.model.api.vegetations.TreeSampling;
import com.google.auto.service.AutoService;

/**
 * Writes and reads {@link TreeSampling} in a byte buffer.
 * 
 * <ul>
 * <li>@{code i} the KID;
 * <li>@{code g} the growth;
 * </ul>
 * 
 * <pre>
 * int   0         1         2         3
 * short 0    1    2    3    4    5    6
 *       iiii iiii gggg
 * </pre>
 */
@AutoService(StoredObjectBuffer.class)
public class TreeSamplingBuffer implements StoredObjectBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = VegetationBuffer.SIZE;

    public static void setTreeSampling(MutableDirectBuffer b, int off, TreeSampling o) {
        VegetationBuffer.writeObject(b, off, o);
    }

    public static TreeSampling getTreeSampling(DirectBuffer b, int off, TreeSampling o) {
        VegetationBuffer.readObject(b, off, o);
        return o;
    }

    @Override
    public StoredObject read(DirectBuffer b) {
        return TreeSamplingBuffer.getTreeSampling(b, 0, new TreeSampling());
    }

    @Override
    public int getObjectType() {
        return TreeSampling.OBJECT_TYPE;
    }

    @Override
    public int getSize(StoredObject go) {
        return SIZE;
    }

    @Override
    public void write(MutableDirectBuffer b, StoredObject go) {
        setTreeSampling(b, 0, (TreeSampling) go);
    }

}
