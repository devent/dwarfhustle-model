package com.anrisoftware.dwarfhustle.model.db.lmbd;

import static java.lang.Math.pow;
import static java.nio.ByteBuffer.allocateDirect;
import static org.lmdbjava.DbiFlags.MDB_CREATE;
import static org.lmdbjava.DbiFlags.MDB_INTEGERKEY;
import static org.lmdbjava.DirectBufferProxy.PROXY_DB;
import static org.lmdbjava.Env.create;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;

/**
 * LMBD storage for game objects that are not on the game map.
 */
public class GameObjectsLmbdStorage implements AutoCloseable, ObjectsGetter {

    private final Env<DirectBuffer> env;

    private final Map<String, Dbi<DirectBuffer>> dbs;

    private final Txn<DirectBuffer> readTxn;

    private final ThreadLocal<UnsafeBuffer> buff8;

    private final IntObjectMap<Function<DirectBuffer, GameObject>> typeReadBuffers;

    /**
     * Creates or opens the game objects storage.
     */
    public GameObjectsLmbdStorage(Path file, int dbcount,
            IntObjectMap<Function<DirectBuffer, GameObject>> typeReadBuffers, Function<Integer, String> dbsNames) {
        this.typeReadBuffers = typeReadBuffers;
        this.env = create(PROXY_DB).setMapSize((long) (10 * pow(10, 9))).setMaxDbs(20).open(file.toFile());
        this.dbs = Maps.mutable.withInitialCapacity(dbcount);
        for (int i = 0; i < dbcount; i++) {
            String name = dbsNames.apply(i);
            dbs.put(name, env.openDbi(name, MDB_CREATE, MDB_INTEGERKEY));
        }
        this.readTxn = env.txnRead();
        readTxn.reset();
        this.buff8 = ThreadLocal.withInitial(() -> new UnsafeBuffer(allocateDirect(8)));
    }

    /**
     * Closes the storage.
     */
    @Override
    public void close() {
        readTxn.close();
        env.close();
    }

    /**
     * Stores the game object in the database.
     */
    public void putObject(String type, long id, int size, Consumer<MutableDirectBuffer> writeBuffer) {
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var key = buff8.get();
            key.putLong(0, id);
            final var val = new UnsafeBuffer(allocateDirect(size));
            writeBuffer.accept(val);
            dbs.get(type).put(txn, key, val);
            txn.commit();
        }
    }

    private class ObjectsListRecursiveAction extends AbstractObjectsListRecursiveAction {

        private static final long serialVersionUID = 1L;

        private final String type;

        public ObjectsListRecursiveAction(int max, int size, int start, int end, List<GameMapObject> objects,
                BiConsumer<GameMapObject, MutableDirectBuffer> writeBuffer, String type) {
            super(max, size, start, end, objects, writeBuffer);
            this.type = type;
        }

        @Override
        protected void processing() {
            try (Txn<DirectBuffer> txn = env.txnWrite()) {
                final var key = buff8.get();
                final var val = new UnsafeBuffer(allocateDirect(size));
                final var c = dbs.get(type).openCursor(txn);
                for (int i = start; i < end; i++) {
                    var o = objects.get(i);
                    key.putLong(0, o.id);
                    writeBuffer.accept(o, val);
                    c.put(key, val);
                }
                txn.commit();
            }
        }

        @Override
        protected AbstractObjectsListRecursiveAction create(int max, int size, int start, int end,
                List<GameMapObject> objects, BiConsumer<GameMapObject, MutableDirectBuffer> writeBuffer) {
            return new ObjectsListRecursiveAction(max, size, start, end, objects, writeBuffer, type);
        }
    }

    /**
     * Mass storage for game objects.
     */
    public void putObjects(String type, int size, List<GameMapObject> objects,
            BiConsumer<GameMapObject, MutableDirectBuffer> writeBuffer) {
        int max = 8192;
        if (objects.size() < max) {
            putObjects(type, size, (Iterable<GameMapObject>) objects, writeBuffer);
        } else {
            var pool = ForkJoinPool.commonPool();
            pool.invoke(new ObjectsListRecursiveAction(max, size, 0, objects.size(), objects, writeBuffer, type));
        }
    }

    /**
     * Mass storage for game objects.
     */
    public void putObjects(String type, int size, Iterable<GameMapObject> objects,
            BiConsumer<GameMapObject, MutableDirectBuffer> writeBuffer) {
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var key = buff8.get();
            final var val = new UnsafeBuffer(allocateDirect(size));
            final var c = dbs.get(type).openCursor(txn);
            for (var o : objects) {
                key.putLong(0, o.id);
                writeBuffer.accept(o, val);
                c.put(key, val);
            }
            txn.commit();
        }
    }

    /**
     * Retrieves the game map with the specific object ID from the database.
     */
    public <T extends GameObject> T getObject(String type, long id, Function<DirectBuffer, T> readBuffer) {
        try {
            final var key = buff8.get();
            readTxn.renew();
            key.putLong(0, id);
            var val = dbs.get(type).get(readTxn, key);
            return readBuffer.apply(val);
        } finally {
            readTxn.reset();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends GameObject> T get(Class<T> typeClass, String type, Object key) {
        return (T) getObject(type, (long) key, typeReadBuffers.get(type.hashCode()));
    }

}
