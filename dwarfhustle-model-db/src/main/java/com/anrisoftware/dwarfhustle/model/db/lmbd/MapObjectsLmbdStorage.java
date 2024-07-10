package com.anrisoftware.dwarfhustle.model.db.lmbd;

import static java.lang.Math.pow;
import static java.nio.ByteBuffer.allocateDirect;
import static org.lmdbjava.DbiFlags.MDB_CREATE;
import static org.lmdbjava.DbiFlags.MDB_DUPFIXED;
import static org.lmdbjava.DbiFlags.MDB_DUPSORT;
import static org.lmdbjava.DbiFlags.MDB_INTEGERDUP;
import static org.lmdbjava.DbiFlags.MDB_INTEGERKEY;
import static org.lmdbjava.DirectBufferProxy.PROXY_DB;
import static org.lmdbjava.Env.create;
import static org.lmdbjava.GetOp.MDB_SET;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;

/**
 * LMBD storage for game map objects on the {@link GameMap}.
 */
public class MapObjectsLmbdStorage implements AutoCloseable, ObjectsGetter {

    private final Env<DirectBuffer> env;

    private final Dbi<DirectBuffer> dbPosIds;

    private final int w;

    private final int h;

    private final int d;

    private final Txn<DirectBuffer> readTxn;

    private final Dbi<DirectBuffer> dbIdObject;

    private final ThreadLocal<UnsafeBuffer> buff4;

    private final ThreadLocal<UnsafeBuffer> buff8;

    private final IntObjectMap<Function<DirectBuffer, GameObject>> typeReadBuffers;

    /**
     * Creates or opens the game map objects storage for the game map.
     */
    public MapObjectsLmbdStorage(Path file, GameMap gm,
            IntObjectMap<Function<DirectBuffer, GameObject>> typeReadBuffers) {
        this.typeReadBuffers = typeReadBuffers;
        this.w = gm.width;
        this.h = gm.height;
        this.d = gm.depth;
        this.env = create(PROXY_DB).setMapSize((long) (10 * pow(10, 9))).setMaxDbs(2).open(file.toFile());
        this.dbPosIds = env.openDbi("pos-ids", MDB_CREATE, MDB_DUPSORT, MDB_DUPFIXED, MDB_INTEGERDUP);
        this.dbIdObject = env.openDbi("id-object", MDB_CREATE, MDB_INTEGERKEY);
        this.readTxn = env.txnRead();
        this.buff4 = ThreadLocal.withInitial(() -> new UnsafeBuffer(allocateDirect(4)));
        this.buff8 = ThreadLocal.withInitial(() -> new UnsafeBuffer(allocateDirect(8)));
        readTxn.reset();
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
     * Stores the game map object in the (x,y,z) block in the database.
     */
    public void putObject(int x, int y, int z, int size, long id, Consumer<MutableDirectBuffer> writeBuffer) {
        int index = GameBlockPos.calcIndex(w, h, d, 0, 0, 0, x, y, z);
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var key = buff4.get();
            final var val = buff8.get();
            key.putInt(0, index);
            val.putLong(0, id);
            dbPosIds.put(txn, key, val);
            txn.commit();
        }
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var key = buff8.get();
            key.putLong(0, id);
            final var val = new UnsafeBuffer(allocateDirect(size));
            writeBuffer.accept(val);
            dbIdObject.put(txn, key, val);
            txn.commit();
        }
    }

    private class ObjectsListRecursiveAction extends AbstractObjectsListRecursiveAction {

        private static final long serialVersionUID = 1L;

        public ObjectsListRecursiveAction(int max, int size, int start, int end, List<GameMapObject> objects,
                BiConsumer<GameMapObject, MutableDirectBuffer> writeBuffer) {
            super(max, size, start, end, objects, writeBuffer);
        }

        @Override
        protected void processing() {
            try (Txn<DirectBuffer> txn = env.txnWrite()) {
                final var c = dbPosIds.openCursor(txn);
                final var key = buff4.get();
                final var val = buff8.get();
                for (int i = start; i < end; i++) {
                    var o = objects.get(i);
                    int index = GameBlockPos.calcIndex(w, h, d, 0, 0, 0, o.pos.x, o.pos.y, o.pos.z);
                    key.putInt(0, index);
                    val.putLong(0, o.id);
                    c.put(key, val);
                }
                txn.commit();
            }
            try (Txn<DirectBuffer> txn = env.txnWrite()) {
                final var key = buff8.get();
                final var val = new UnsafeBuffer(allocateDirect(size));
                final var c = dbPosIds.openCursor(txn);
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
            return new ObjectsListRecursiveAction(max, size, start, end, objects, writeBuffer);
        }
    }

    /**
     * Mass storage for game map objects.
     */
    public void putObjects(int size, List<GameMapObject> objects,
            BiConsumer<GameMapObject, MutableDirectBuffer> writeBuffer) {
        int max = 8192;
        if (objects.size() < max) {
            putObjects(size, (Iterable<GameMapObject>) objects, writeBuffer);
        } else {
            var pool = ForkJoinPool.commonPool();
            pool.invoke(new ObjectsListRecursiveAction(max, size, 0, objects.size(), objects, writeBuffer));
        }
    }

    /**
     * Mass storage for game map objects.
     */
    public void putObjects(int size, Iterable<GameMapObject> objects,
            BiConsumer<GameMapObject, MutableDirectBuffer> writeBuffer) {
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var c = dbPosIds.openCursor(txn);
            final var key = buff4.get();
            final var val = buff8.get();
            for (var o : objects) {
                int index = GameBlockPos.calcIndex(w, h, d, 0, 0, 0, o.pos.x, o.pos.y, o.pos.z);
                key.putInt(0, index);
                val.putLong(0, o.id);
                c.put(key, val);
            }
            txn.commit();
        }
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var key = buff8.get();
            final var val = new UnsafeBuffer(allocateDirect(size));
            final var c = dbPosIds.openCursor(txn);
            for (var o : objects) {
                key.putLong(0, o.id);
                writeBuffer.accept(o, val);
                c.put(key, val);
            }
            txn.commit();
        }
    }

    /**
     * Retrieves the game map object with the specific object ID from the database.
     */
    public <T extends GameObject> T getObject(long id, Function<DirectBuffer, T> readBuffer) {
        try {
            readTxn.renew();
            final var key = buff8.get();
            key.putLong(0, id);
            var val = dbIdObject.get(readTxn, key);
            return readBuffer.apply(val);
        } finally {
            readTxn.reset();
        }
    }

    /**
     * Retrieves the game map objects on the (x,y,z) block from the database.
     */
    public <T extends GameObject> void getObjects(int x, int y, int z, Function<DirectBuffer, T> readBuffer,
            Consumer<T> consumer) {
        try {
            readTxn.renew();
            final var key = buff4.get();
            final int index = GameBlockPos.calcIndex(w, h, d, 0, 0, 0, x, y, z);
            key.putInt(0, index);
            final var c = dbPosIds.openCursor(readTxn);
            if (!c.get(key, MDB_SET)) {
                return;
            }
            for (int i = 0; i < c.count(); i++) {
                var bid = c.val();
                var val = dbPosIds.get(readTxn, bid);
                var o = readBuffer.apply(val);
                consumer.accept(o);
                c.next();
            }
        } finally {
            readTxn.reset();
        }
    }

    /**
     * Retrieves the game map objects from a range start (x,y,z) to end (x,y,z)
     * blocks from the database.
     */
    public <T extends GameObject> void getObjectsRange(int sx, int sy, int sz, int ex, int ey, int ez,
            Function<DirectBuffer, T> readBuffer, Consumer<T> consumer) {
        final int xx = ex - sx;
        final int yy = ey - sy;
        final int zz = ez - sz;
        try {
            readTxn.renew();
            final var key = buff4.get();
            final var c = dbPosIds.openCursor(readTxn);
            for (int x = sx; x < xx; x++) {
                for (int y = sy; y < yy; y++) {
                    for (int z = sz; z < zz; z++) {
                        final int index = GameBlockPos.calcIndex(w, h, d, 0, 0, 0, x, y, z);
                        key.putInt(0, index);
                        if (!c.get(key, MDB_SET)) {
                            return;
                        }
                        for (int i = 0; i < c.count(); i++) {
                            var bid = c.val();
                            var val = dbPosIds.get(readTxn, bid);
                            var o = readBuffer.apply(val);
                            consumer.accept(o);
                            c.next();
                        }
                    }
                }
            }
        } finally {
            readTxn.reset();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends GameObject> T get(Class<T> typeClass, String type, Object key) {
        return (T) getObject((long) key, typeReadBuffers.get(type.hashCode()));
    }

}
