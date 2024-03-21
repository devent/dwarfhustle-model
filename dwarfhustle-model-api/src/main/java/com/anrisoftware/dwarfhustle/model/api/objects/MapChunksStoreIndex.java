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
package com.anrisoftware.dwarfhustle.model.api.objects;

import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.readExternalLongObjectMap;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.readStreamLongObjectMap;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.writeExternalLongObjectMap;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.writeStreamLongObjectMap;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.eclipse.collections.api.factory.primitive.LongObjectMaps;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Positions and sizes of the {@link MapChunk}s stored in the
 * {@link MapChunksStore}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString
public class MapChunksStoreIndex implements Externalizable, StreamStorage {

    /**
     * Returns the size of the index in bytes.
     */
    public static int getSizeObjectStream(int count) {
        return 236 + 24 * (count - 1);
    }

    /**
     * Returns the size of the index in bytes.
     */
    public static int getSizeDataStream(int count) {
        return 36 + 16 * (count - 1);
    }

    /**
     * Index entry with the position and size of the {@link MapChunk} in bytes.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @SuppressWarnings("serial")
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class Index implements Serializable, StreamStorage {

        public int pos;

        public int size;

        @Override
        public void writeStream(DataOutput out) throws IOException {
            out.writeInt(pos);
            out.writeInt(size);
        }

        @Override
        public void readStream(DataInput in) throws IOException {
            this.pos = in.readInt();
            this.size = in.readInt();
        }
    }

    /**
     * Stores the index of Chunk-ID := {@link Index}.
     */
    public MutableLongObjectMap<Index> map;

    public MapChunksStoreIndex(int count) {
        this.map = LongObjectMaps.mutable.ofInitialCapacity(count);
        // put -1 to avoid a if-branch
        this.map.put(-1, new Index(getSizeDataStream(count), 0));
        for (int i = 0; i < count; i++) {
            this.map.put(i, new Index(0, 0));
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        writeExternalLongObjectMap(out, map);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.map = readExternalLongObjectMap(in);
    }

    @Override
    public void writeStream(DataOutput out) throws IOException {
        writeStreamLongObjectMap(out, map);
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        this.map = readStreamLongObjectMap(in, Index::new);
    }

}
