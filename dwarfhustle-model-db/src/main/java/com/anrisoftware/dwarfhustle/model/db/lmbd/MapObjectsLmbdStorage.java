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
package com.anrisoftware.dwarfhustle.model.db.lmbd;

import static com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos.calcIndex;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.eclipse.collections.api.LongIterable;
import org.eclipse.collections.api.list.primitive.LongList;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.MapObjectsStorage;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsConsumer;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsSetter;
import com.anrisoftware.dwarfhustle.model.db.cache.MapObject;
import com.google.inject.assistedinject.Assisted;

import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

/**
 * Store the object ID and object type for the (x,y,z) map block.
 */
public class MapObjectsLmbdStorage implements MapObjectsStorage, ObjectsGetter, ObjectsSetter {

    /**
     * Factory to create the {@link MapObjectsLmbdStorage}.
     * 
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface MapObjectsLmbdStorageFactory {
        MapObjectsLmbdStorage create(Path file, GameMap gm);
    }

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
    @Inject
    protected MapObjectsLmbdStorage(@Assisted Path file, @Assisted GameMap gm) {
        this.w = gm.width;
        this.h = gm.height;
        this.d = gm.depth;
        this.env = create(PROXY_DB).setMapSize((long) (10 * pow(10, 9))).setMaxDbs(1).open(file.toFile());
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
    @Override
    public void putObject(int x, int y, int z, int type, long id) {
        int index = GameBlockPos.calcIndex(w, h, d, 0, 0, 0, x, y, z);
        putObject(index, type, id);
    }

    private void putObject(int index, int type, long id) {
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

    @RequiredArgsConstructor
    private class ObjectsListRecursiveAction extends RecursiveAction {

        private static final long serialVersionUID = 1L;

        protected final int max;

        protected final int start;

        protected final int end;

        protected final int index;

        protected final int type;

        protected final LongList oids;

        @Override
        protected void compute() {
            if (end - start > max) {
                ForkJoinTask.invokeAll(createSubtasks());
            } else {
                processing();
            }
        }

        private Collection<ObjectsListRecursiveAction> createSubtasks() {
            List<ObjectsListRecursiveAction> dividedTasks = new ArrayList<>();
            dividedTasks.add(create(max, start, start / 2 + end / 2));
            dividedTasks.add(create(max, start / 2 + end / 2, end));
            return dividedTasks;
        }

        protected void processing() {
            try (Txn<DirectBuffer> txn = env.txnWrite()) {
                final var c = db.openCursor(txn);
                final var key = buffkey.get();
                final var val = buffval.get();
                for (int i = start; i < end; i++) {
                    var id = oids.get(i);
                    key.putInt(0, index);
                    MapObjectValue.setId(val, 0, id);
                    MapObjectValue.setType(val, 0, type);
                    c.put(key, val);
                }
                txn.commit();
            }
        }

        protected ObjectsListRecursiveAction create(int max, int start, int end) {
            return new ObjectsListRecursiveAction(max, start, end, index, type, oids);
        }
    }

    /**
     * Mass storage for game map objects.
     */
    @Override
    public void putObjects(int index, int type, LongIterable ids) {
        int max = 8192;
        if (ids instanceof LongList list) {
            var pool = ForkJoinPool.commonPool();
            pool.invoke(new ObjectsListRecursiveAction(max, 0, list.size(), index, type, list));
        } else {
            putObjects0(index, type, ids);
        }
    }

    /**
     * Mass storage for game map objects.
     */
    public void putObjects0(int index, int type, LongIterable ids) {
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var c = db.openCursor(txn);
            final var key = buffkey.get();
            final var val = buffval.get();
            for (var it = ids.longIterator(); it.hasNext();) {
                key.putInt(0, index);
                MapObjectValue.setId(val, 0, it.next());
                MapObjectValue.setType(val, 0, type);
                c.put(key, val);
            }
            txn.commit();
        }
    }

    /**
     * Retrieves the game map objects on the (x,y,z) block from the database.
     */
    @Override
    public void getObjects(int x, int y, int z, ObjectsConsumer consumer) {
        final int index = calcIndex(w, h, d, 0, 0, 0, x, y, z);
        getObjects(x, y, z, index, consumer);
    }

    private synchronized void getObjects(int x, int y, int z, int index, ObjectsConsumer consumer) {
        try {
            readTxn.renew();
            final var key = buffkey.get();
            key.putInt(0, index);
            final var c = db.openCursor(readTxn);
            if (!c.get(key, MDB_SET)) {
                return;
            }
            for (int i = 0; i < c.count(); i++) {
                var val = c.val();
                long id = MapObjectValue.getId(val, 0);
                int type = MapObjectValue.getType(val, 0);
                consumer.accept(type, id, x, y, z);
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
    @Override
    public synchronized void getObjectsRange(int sx, int sy, int sz, int ex, int ey, int ez, ObjectsConsumer consumer) {
        try {
            readTxn.renew();
            final var key = buffkey.get();
            final var c = db.openCursor(readTxn);
            for (int x = sx; x < ex; x++) {
                for (int y = sy; y < ey; y++) {
                    for (int z = sz; z < ez; z++) {
                        final int index = calcIndex(w, h, d, 0, 0, 0, x, y, z);
                        key.putInt(0, index);
                        if (!c.get(key, MDB_SET)) {
                            continue;
                        }
                        for (int i = 0; i < c.count(); i++) {
                            var val = c.val();
                            long id = MapObjectValue.getId(val, 0);
                            int type = MapObjectValue.getType(val, 0);
                            consumer.accept(type, id, x, y, z);
                            c.next();
                        }
                    }
                }
            }
        } finally {
            readTxn.reset();
        }
    }

    @Override
    public void removeObject(int x, int y, int z, int type, long id) {
        int index = calcIndex(w, h, d, 0, 0, 0, x, y, z);
        removeObject(type, index, id);
    }

    private void removeObject(int type, int index, long id) {
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var key = buffkey.get();
            final var val = buffval.get();
            key.putInt(0, index);
            MapObjectValue.setId(val, 0, id);
            MapObjectValue.setType(val, 0, type);
            db.delete(txn, key, val);
            txn.commit();
        }
    }

    @Override
    public void set(int type, GameObject go) throws ObjectsSetterException {
        var mo = (MapObject) go;
        mo.getOids().forEachKeyValue((id, type0) -> putObject(mo.getIndex(), type0, id));
    }

    @Override
    public void set(int type, Iterable<GameObject> values) throws ObjectsSetterException {
        for (var go : values) {
            var mo = (MapObject) go;
            mo.getOids().forEachKeyValue((id, type0) -> putObject(mo.getIndex(), type0, id));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends GameObject> T get(int type, long key) throws ObjectsGetterException {
        final int index = (int) key;
        var mo = new MapObject(index);
        getObjects(0, 0, 0, index, (type0, id, x, y, z) -> {
            mo.addObject(type0, id);
        });
        return (T) mo;
    }

    @Override
    public void remove(int type, GameObject go) throws ObjectsSetterException {
        var mo = (MapObject) go;
        mo.getOids().forEachKeyValue((id, type0) -> removeObject(mo.getIndex(), type0, id));
    }
}
