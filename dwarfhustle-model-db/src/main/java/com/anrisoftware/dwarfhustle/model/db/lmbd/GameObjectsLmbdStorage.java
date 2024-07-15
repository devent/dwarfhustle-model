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

import static java.lang.Math.pow;
import static java.nio.ByteBuffer.allocateDirect;
import static org.lmdbjava.DbiFlags.MDB_CREATE;
import static org.lmdbjava.DbiFlags.MDB_INTEGERKEY;
import static org.lmdbjava.DirectBufferProxy.PROXY_DB;
import static org.lmdbjava.Env.create;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.set.primitive.IntSet;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsSetter;

/**
 * Stores {@link GameObject}(s).
 */
public class GameObjectsLmbdStorage implements AutoCloseable, ObjectsGetter, ObjectsSetter {

    private final Env<DirectBuffer> env;

    private final IntObjectMap<Dbi<DirectBuffer>> dbs;

    private final Txn<DirectBuffer> readTxn;

    private final ThreadLocal<UnsafeBuffer> buff8;

    private final IntObjectMap<Function<DirectBuffer, GameObject>> typeReadBuffers;

    /**
     * Creates or opens the game objects storage.
     */
    public GameObjectsLmbdStorage(Path file, IntSet objectTypes,
            IntObjectMap<Function<DirectBuffer, GameObject>> typeReadBuffers) {
        this.typeReadBuffers = typeReadBuffers;
        this.env = create(PROXY_DB).setMapSize((long) (10 * pow(10, 9))).setMaxDbs(20).open(file.toFile());
        MutableIntObjectMap<Dbi<DirectBuffer>> dbs = IntObjectMaps.mutable.withInitialCapacity(objectTypes.size());
        objectTypes.each((type) -> {
            dbs.put(type, env.openDbi(Integer.toString(type), MDB_CREATE, MDB_INTEGERKEY));
        });
        this.dbs = dbs;
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
    public void putObject(int type, long id, int size, Consumer<MutableDirectBuffer> writeBuffer) {
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

        private final int size;

        private final int type;

        protected final BiConsumer<GameMapObject, MutableDirectBuffer> writeBuffer;

        public ObjectsListRecursiveAction(int max, int start, int end, List<GameMapObject> objects, int size, int type,
                BiConsumer<GameMapObject, MutableDirectBuffer> writeBuffer) {
            super(max, start, end, objects);
            this.size = size;
            this.type = type;
            this.writeBuffer = writeBuffer;
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
        protected AbstractObjectsListRecursiveAction create(int max, int start, int end, List<GameMapObject> objects) {
            return new ObjectsListRecursiveAction(max, start, end, objects, size, type, writeBuffer);
        }
    }

    /**
     * Mass storage for game objects.
     */
    public void putObjects(int type, int size, List<GameMapObject> objects,
            BiConsumer<GameMapObject, MutableDirectBuffer> writeBuffer) {
        int max = 8192;
        if (objects.size() < max) {
            putObjects(type, size, (Iterable<GameMapObject>) objects, writeBuffer);
        } else {
            var pool = ForkJoinPool.commonPool();
            pool.invoke(new ObjectsListRecursiveAction(max, 0, objects.size(), objects, size, type, writeBuffer));
        }
    }

    /**
     * Mass storage for game objects.
     */
    public void putObjects(int type, int size, Iterable<GameMapObject> objects,
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
    @SuppressWarnings("unchecked")
    public <T extends GameObject> T getObject(int type, long id) {
        try {
            final var key = buff8.get();
            readTxn.renew();
            key.putLong(0, id);
            var val = dbs.get(type).get(readTxn, key);
            return (T) typeReadBuffers.get(type).apply(val);
        } finally {
            readTxn.reset();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends GameObject> T get(int type, Object key) {
        return (T) getObject(type, (long) key);
    }

    @Override
    public void set(int type, GameObject go) throws ObjectsSetterException {
        // TODO Auto-generated method stub

    }

    @Override
    public void set(int type, Iterable<GameObject> values) throws ObjectsSetterException {
        // TODO Auto-generated method stub

    }

}
