package com.anrisoftware.dwarfhustle.model.api.objects;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.util.function.Supplier;

import org.eclipse.collections.api.factory.primitive.IntLongMaps;
import org.eclipse.collections.api.factory.primitive.ObjectLongMaps;
import org.eclipse.collections.api.map.primitive.IntLongMap;
import org.eclipse.collections.api.map.primitive.MutableIntLongMap;
import org.eclipse.collections.api.map.primitive.MutableObjectLongMap;
import org.eclipse.collections.api.map.primitive.ObjectLongMap;

/**
 * Utils to write/read external.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class ExternalizableUtils {

    /**
     * Read a {@link IntLongMap}.
     */
    public static IntLongMap readExternalIntLongMap(ObjectInput in) throws IOException {
        int size = in.readInt();
        MutableIntLongMap map = IntLongMaps.mutable.ofInitialCapacity(size);
        for (int i = 0; i < size; i++) {
            map.put(in.readInt(), in.readLong());
        }
        return map;
    }

    /**
     * Read a {@link ObjectLongMap}.
     */
    public static <T> ObjectLongMap<T> readExternalObjectLongMap(ObjectInput in, Supplier<T> supply)
            throws IOException, ClassNotFoundException {
        int size = in.readInt();
        MutableObjectLongMap<T> map = ObjectLongMaps.mutable.ofInitialCapacity(size);
        for (int i = 0; i < size; i++) {
            var key = supply.get();
            var keyex = (Externalizable) key;
            keyex.readExternal(in);
            map.put(key, in.readLong());
        }
        return map;
    }

}
