package com.anrisoftware.dwarfhustle.model.db.lmbd;

import static java.lang.Math.pow;
import static java.nio.ByteBuffer.allocateDirect;
import static org.lmdbjava.DbiFlags.MDB_CREATE;
import static org.lmdbjava.DbiFlags.MDB_DUPFIXED;
import static org.lmdbjava.DbiFlags.MDB_DUPSORT;
import static org.lmdbjava.DbiFlags.MDB_INTEGERDUP;
import static org.lmdbjava.DirectBufferProxy.PROXY_DB;
import static org.lmdbjava.Env.create;
import static org.lmdbjava.GetOp.MDB_SET;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;

/**
 * Store the object ID and object type for the (x,y,z) map block.
 */
public class MapObjectsLmbdStorage implements AutoCloseable {

    private final Env<DirectBuffer> env;

    private final Dbi<DirectBuffer> db;

    private final int w;

    private final int h;

    private final int d;

    private final Txn<DirectBuffer> readTxn;

    private final ThreadLocal<UnsafeBuffer> buffkey;

    private final ThreadLocal<UnsafeBuffer> buffval;

    /**
     * Creates or opens the game map objects storage for the game map.
     */
    public MapObjectsLmbdStorage(Path file, GameMap gm) {
        this.w = gm.width;
        this.h = gm.height;
        this.d = gm.depth;
        this.env = create(PROXY_DB).setMapSize((long) (10 * pow(10, 9))).setMaxDbs(2).open(file.toFile());
        this.db = env.openDbi("pos-ids", MDB_CREATE, MDB_DUPSORT, MDB_DUPFIXED, MDB_INTEGERDUP);
        this.readTxn = env.txnRead();
        this.buffkey = ThreadLocal.withInitial(() -> new UnsafeBuffer(allocateDirect(4)));
        this.buffval = ThreadLocal.withInitial(() -> new UnsafeBuffer(allocateDirect(8 + 4)));
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
    public void putObject(int x, int y, int z, int type, long id) {
        int index = GameBlockPos.calcIndex(w, h, d, 0, 0, 0, x, y, z);
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var key = buffkey.get();
            final var val = buffval.get();
            key.putInt(0, index);
            MapObjectValue.setId(val, 0, id);
            MapObjectValue.setType(val, 0, type);
            db.put(txn, key, val);
            txn.commit();
        }
    }

    private class ObjectsListRecursiveAction extends AbstractObjectsListRecursiveAction {

        private static final long serialVersionUID = 1L;

        public ObjectsListRecursiveAction(int max, int start, int end, List<GameMapObject> objects) {
            super(max, start, end, objects);
        }

        @Override
        protected void processing() {
            try (Txn<DirectBuffer> txn = env.txnWrite()) {
                final var c = db.openCursor(txn);
                final var key = buffkey.get();
                final var val = buffval.get();
                for (int i = start; i < end; i++) {
                    var o = objects.get(i);
                    int index = GameBlockPos.calcIndex(w, h, d, 0, 0, 0, o.pos.x, o.pos.y, o.pos.z);
                    key.putInt(0, index);
                    MapObjectValue.setId(val, 0, o.id);
                    MapObjectValue.setType(val, 0, o.getObjectType());
                    c.put(key, val);
                }
                txn.commit();
            }
        }

        @Override
        protected AbstractObjectsListRecursiveAction create(int max, int start, int end, List<GameMapObject> objects) {
            return new ObjectsListRecursiveAction(max, start, end, objects);
        }
    }

    /**
     * Mass storage for game map objects.
     */
    public void putObjects(List<GameMapObject> objects) {
        int max = 8192;
        if (objects.size() < max) {
            putObjects((Iterable<GameMapObject>) objects);
        } else {
            var pool = ForkJoinPool.commonPool();
            pool.invoke(new ObjectsListRecursiveAction(max, 0, objects.size(), objects));
        }
    }

    /**
     * Mass storage for game map objects.
     */
    public void putObjects(Iterable<GameMapObject> objects) {
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var c = db.openCursor(txn);
            final var key = buffkey.get();
            final var val = buffval.get();
            for (var o : objects) {
                int index = GameBlockPos.calcIndex(w, h, d, 0, 0, 0, o.pos.x, o.pos.y, o.pos.z);
                key.putInt(0, index);
                MapObjectValue.setId(val, 0, o.id);
                MapObjectValue.setType(val, 0, o.getObjectType());
                c.put(key, val);
            }
            txn.commit();
        }
    }

    /**
     * Retrieves the game map objects on the (x,y,z) block from the database.
     */
    public void getObjects(int x, int y, int z, BiConsumer<Integer, Long> consumer) {
        try {
            readTxn.renew();
            final var key = buffkey.get();
            final int index = GameBlockPos.calcIndex(w, h, d, 0, 0, 0, x, y, z);
            key.putInt(0, index);
            final var c = db.openCursor(readTxn);
            if (!c.get(key, MDB_SET)) {
                return;
            }
            for (int i = 0; i < c.count(); i++) {
                var val = c.val();
                long id = MapObjectValue.getId(val, 0);
                int type = MapObjectValue.getType(val, 0);
                consumer.accept(type, id);
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
    public void getObjectsRange(int sx, int sy, int sz, int ex, int ey, int ez, BiConsumer<Integer, Long> consumer) {
        final int xx = ex - sx;
        final int yy = ey - sy;
        final int zz = ez - sz;
        try {
            readTxn.renew();
            final var key = buffkey.get();
            final var c = db.openCursor(readTxn);
            for (int x = sx; x < xx; x++) {
                for (int y = sy; y < yy; y++) {
                    for (int z = sz; z < zz; z++) {
                        final int index = GameBlockPos.calcIndex(w, h, d, 0, 0, 0, x, y, z);
                        key.putInt(0, index);
                        if (!c.get(key, MDB_SET)) {
                            return;
                        }
                        for (int i = 0; i < c.count(); i++) {
                            var val = c.val();
                            long id = MapObjectValue.getId(val, 0);
                            int type = MapObjectValue.getType(val, 0);
                            consumer.accept(type, id);
                            c.next();
                        }
                    }
                }
            }
        } finally {
            readTxn.reset();
        }
    }
}
