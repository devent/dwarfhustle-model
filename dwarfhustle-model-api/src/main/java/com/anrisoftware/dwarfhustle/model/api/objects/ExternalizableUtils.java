package com.anrisoftware.dwarfhustle.model.api.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.function.Supplier;

import org.eclipse.collections.api.factory.primitive.IntLongMaps;
import org.eclipse.collections.api.factory.primitive.LongObjectMaps;
import org.eclipse.collections.api.factory.primitive.ObjectIntMaps;
import org.eclipse.collections.api.factory.primitive.ObjectLongMaps;
import org.eclipse.collections.api.map.primitive.IntLongMap;
import org.eclipse.collections.api.map.primitive.LongObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntLongMap;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;
import org.eclipse.collections.api.map.primitive.MutableObjectLongMap;
import org.eclipse.collections.api.map.primitive.ObjectIntMap;
import org.eclipse.collections.api.map.primitive.ObjectLongMap;

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
        out.writeInt(map.size());
        for (var view : map.keyValuesView()) {
            out.writeLong(view.getOne());
            view.getTwo().writeStream(out);
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

}
