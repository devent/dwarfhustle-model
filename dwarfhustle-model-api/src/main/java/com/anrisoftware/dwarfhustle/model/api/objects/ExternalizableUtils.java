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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.collections.api.LongIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.primitive.IntLongMaps;
import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.factory.primitive.LongIntMaps;
import org.eclipse.collections.api.factory.primitive.LongLists;
import org.eclipse.collections.api.factory.primitive.LongObjectMaps;
import org.eclipse.collections.api.factory.primitive.ObjectIntMaps;
import org.eclipse.collections.api.factory.primitive.ObjectLongMaps;
import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.api.map.primitive.IntLongMap;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.api.map.primitive.LongIntMap;
import org.eclipse.collections.api.map.primitive.LongObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntLongMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableLongIntMap;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;
import org.eclipse.collections.api.map.primitive.MutableObjectLongMap;
import org.eclipse.collections.api.map.primitive.ObjectIntMap;
import org.eclipse.collections.api.map.primitive.ObjectLongMap;
import org.eclipse.collections.api.multimap.MutableMultimap;

/**
 * Utils to write/read external.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class ExternalizableUtils {

    /**
     * Read a {@link IntLongMap}. This creates a new map with initial capacity of
     * size to avoid a re-hash of the map.
     */
    public static IntLongMap readExternalIntLongMap(ObjectInput in) throws IOException {
        return readStreamIntLongMap(in);
    }

    /**
     * Read a {@link ObjectLongMap}.
     */
    @SuppressWarnings("unchecked")
    public static <T> MutableObjectLongMap<T> readExternalObjectLongMap(ObjectInput in)
            throws IOException, ClassNotFoundException {
        int size = in.readInt();
        MutableObjectLongMap<T> map = ObjectLongMaps.mutable.ofInitialCapacity(size);
        for (int i = 0; i < size; i++) {
            map.put((T) in.readObject(), in.readLong());
        }
        return map;
    }

    /**
     * Writes a {@link LongObjectMap}. Workaround to avoid a re-hash of the map each
     * time an entry is red.
     */
    public static void writeExternalLongObjectMap(ObjectOutput out, LongObjectMap<?> map) throws IOException {
        out.writeInt(map.size());
        for (var view : map.keyValuesView()) {
            out.writeLong(view.getOne());
            out.writeObject(view.getTwo());
        }
    }

    /**
     * Reads a {@link ObjectIntMap}. Workaround to avoid a re-hash of the map each
     * time an entry is red.
     */
    @SuppressWarnings("unchecked")
    public static <T> MutableLongObjectMap<T> readExternalLongObjectMap(ObjectInput in)
            throws IOException, ClassNotFoundException {
        int size = in.readInt();
        MutableLongObjectMap<T> map = LongObjectMaps.mutable.ofInitialCapacity(size);
        for (int i = 0; i < size; i++) {
            map.put(in.readLong(), (T) in.readObject());
        }
        return map;
    }

    /**
     * Read a {@link ObjectIntMap}.
     */
    @SuppressWarnings("unchecked")
    public static <T> MutableObjectIntMap<T> readExternalObjectIntMap(ObjectInput in, Supplier<T> supply)
            throws IOException, ClassNotFoundException {
        int size = in.readInt();
        MutableObjectIntMap<T> map = ObjectIntMaps.mutable.ofInitialCapacity(size);
        for (int i = 0; i < size; i++) {
            map.put((T) in.readObject(), in.readInt());
        }
        return map;
    }

    /**
     * Writes the keys and values of the {@link IntLongMap} to stream.
     */
    public static void writeStreamIntLongMap(DataOutput out, IntLongMap map) throws IOException {
        out.writeInt(map.size());
        for (var view : map.keyValuesView()) {
            out.writeInt(view.getOne());
            out.writeLong(view.getTwo());
        }
    }

    /**
     * Read a {@link IntLongMap}. This creates a new map with initial capacity of
     * size to avoid a re-hash of the map.
     */
    public static IntLongMap readStreamIntLongMap(DataInput in) throws IOException {
        int size = in.readInt();
        MutableIntLongMap map = IntLongMaps.mutable.ofInitialCapacity(size);
        for (int i = 0; i < size; i++) {
            map.put(in.readInt(), in.readLong());
        }
        return map;
    }

    /**
     * Writes the keys and values of the {@link LongObjectMap} to stream.
     */
    public static void writeStreamLongObjectMap(DataOutput out, LongObjectMap<? extends StreamStorage> map)
            throws IOException {
        if (map != null) {
            out.writeInt(map.size());
            for (var view : map.keyValuesView()) {
                out.writeLong(view.getOne());
                view.getTwo().writeStream(out);
            }
        } else {
            out.writeInt(0);
        }
    }

    /**
     * Reads the keys and values of the {@link LongObjectMap} from stream.
     */
    public static <T extends StreamStorage> MutableLongObjectMap<T> readStreamLongObjectMap(DataInput in,
            Supplier<T> supplier) throws IOException {
        int size = in.readInt();
        MutableLongObjectMap<T> map = LongObjectMaps.mutable.ofInitialCapacity(size);
        for (int i = 0; i < size; i++) {
            long key = in.readLong();
            T value = supplier.get();
            value.readStream(in);
            map.put(key, value);
        }
        return map;
    }

    /**
     * Writes the keys and values of the {@link IntObjectMap} to stream.
     */
    public static void writeStreamIntObjectMap(DataOutput out, IntObjectMap<? extends StreamStorage> map)
            throws IOException {
        out.writeInt(map.size());
        for (var view : map.keyValuesView()) {
            out.writeInt(view.getOne());
            view.getTwo().writeStream(out);
        }
    }

    /**
     * Reads the keys and values of the {@link IntObjectMap} from stream.
     */
    public static <T extends StreamStorage> MutableIntObjectMap<T> readStreamIntObjectMap(DataInput in,
            Supplier<T> supplier) throws IOException {
        int size = in.readInt();
        MutableIntObjectMap<T> map = IntObjectMaps.mutable.ofInitialCapacity(size);
        for (int i = 0; i < size; i++) {
            int key = in.readInt();
            T value = supplier.get();
            value.readStream(in);
            map.put(key, value);
        }
        return map;
    }

    /**
     * Writes the keys and values of the {@link IntObjectMap} to stream.
     */
    public static <T> void writeStreamIntObjectMap(DataOutput out, IntObjectMap<T> map, BiConsumer<DataOutput, T> write)
            throws IOException {
        out.writeInt(map.size());
        for (var view : map.keyValuesView()) {
            out.writeInt(view.getOne());
            write.accept(out, view.getTwo());
        }
    }

    /**
     * Reads the keys and values of the {@link IntObjectMap} from stream.
     */
    public static <T> MutableIntObjectMap<T> readStreamIntObjectMap(DataInput in, Function<DataInput, T> read)
            throws IOException {
        int size = in.readInt();
        MutableIntObjectMap<T> map = IntObjectMaps.mutable.ofInitialCapacity(size);
        for (int i = 0; i < size; i++) {
            int key = in.readInt();
            var value = read.apply(in);
            map.put(key, value);
        }
        return map;
    }

    /**
     * Writes the values of any {@link LongIterable}.
     */
    public static void writeStreamLongCollection(DataOutput out, int size, LongIterable it) throws IOException {
        out.writeInt(size);
        for (var i = it.longIterator(); i.hasNext();) {
            out.writeLong(i.next());
        }
    }

    /**
     * Reads the values of to a {@link LongIterable}.
     */
    public static <T extends LongIterable> LongIterable readStreamLongCollection(DataInput in) throws IOException {
        int size = in.readInt();
        MutableLongList list = LongLists.mutable.withInitialCapacity(size);
        for (int i = 0; i < size; i++) {
            list.add(in.readLong());
        }
        return list;
    }

    /**
     * Writes the keys and values of the {@link LongIntMap} to stream.
     */
    public static void writeStreamLongIntMap(DataOutput out, LongIntMap map) throws IOException {
        out.writeInt(map.size());
        for (var view : map.keyValuesView()) {
            out.writeLong(view.getOne());
            out.writeInt(view.getTwo());
        }
    }

    /**
     * Reads the keys and values of the {@link LongIntMap} from stream.
     */
    public static LongIntMap readStreamLongIntMap(DataInput in) throws IOException {
        int size = in.readInt();
        MutableLongIntMap map = LongIntMaps.mutable.ofInitialCapacity(size);
        for (int i = 0; i < size; i++) {
            map.put(in.readLong(), in.readInt());
        }
        return map;
    }

    /**
     * Writes the keys and values of the {@link MutableMultimap} to stream.
     */
    public static void writeExternalMutableLongIntMultimap(DataOutput out, MutableMultimap<Long, Integer> map)
            throws IOException {
        out.writeInt(map.size());
        for (var keysValues : map.keyMultiValuePairsView()) {
            out.writeInt(keysValues.getTwo().size());
            out.writeLong(keysValues.getOne());
            for (var value : keysValues.getTwo()) {
                out.writeInt(value);
            }
        }
    }

    /**
     * Reads the keys and values of the {@link MutableMultimap} from stream.
     */
    public static MutableMultimap<Long, Integer> readExternalMutableLongIntMultimap(DataInput in,
            Supplier<MutableMultimap<Long, Integer>> supplier) throws IOException {
        int size = in.readInt();
        MutableMultimap<Long, Integer> map = supplier.get();
        for (int i = 0; i < size; i++) {
            final int vsize = in.readInt();
            List<Integer> values = Lists.mutable.withInitialCapacity(vsize);
            final long key = in.readLong();
            for (int j = 0; j < vsize; j++) {
                values.add(in.readInt());
            }
            map.putAll(key, values);
        }
        return map;
    }

    /**
     * Writes the keys and values of the {@link MutableMultimap} to stream.
     */
    public static void writeExternalMutableIntIntMultimap(DataOutput out, MutableMultimap<Integer, Integer> map)
            throws IOException {
        out.writeInt(map.size());
        for (var keysValues : map.keyMultiValuePairsView()) {
            out.writeInt(keysValues.getTwo().size());
            out.writeInt(keysValues.getOne());
            for (var value : keysValues.getTwo()) {
                out.writeInt(value);
            }
        }
    }

    /**
     * Reads the keys and values of the {@link MutableMultimap} from stream.
     */
    public static MutableMultimap<Integer, Integer> readExternalMutableIntIntMultimap(DataInput in,
            Supplier<MutableMultimap<Integer, Integer>> supplier) throws IOException {
        int size = in.readInt();
        MutableMultimap<Integer, Integer> map = supplier.get();
        for (int i = 0; i < size; i++) {
            final int vsize = in.readInt();
            List<Integer> values = Lists.mutable.withInitialCapacity(vsize);
            final int key = in.readInt();
            for (int j = 0; j < vsize; j++) {
                values.add(in.readInt());
            }
            map.putAll(key, values);
        }
        return map;
    }

}
