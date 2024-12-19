/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
 * Copyright © 2022-2024 Erwin Müller (erwin.mueller@anrisoftware.com)
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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.set.primitive.IntSet;
import org.lmdbjava.CursorIterable;
import org.lmdbjava.CursorIterable.KeyVal;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObjectsStorage;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.anrisoftware.dwarfhustle.model.db.buffers.StoredObjectBuffer;
import com.google.inject.assistedinject.Assisted;

import jakarta.inject.Inject;

/**
 * Stores {@link GameObject}(s).
 */
public class GameObjectsLmbdStorage implements GameObjectsStorage {

    /**
     * Factory to create the {@link GameObjectsLmbdStorage}.
     * 
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface GameObjectsLmbdStorageFactory {
        GameObjectsLmbdStorage create(Path file);
    }

    private final Env<DirectBuffer> env;

    private final IntObjectMap<Dbi<DirectBuffer>> dbs;

    private final ThreadLocal<UnsafeBuffer> buff8;

    private final IntObjectMap<StoredObjectBuffer> readBuffers;

    /**
     * Creates or opens the game objects storage.
     */
    @Inject
    protected GameObjectsLmbdStorage(@Assisted Path file, IntSet objectTypes,
            IntObjectMap<StoredObjectBuffer> readBuffers) {
        this.readBuffers = readBuffers;
        this.env = create(PROXY_DB).setMapSize((long) (10 * pow(10, 9))).setMaxDbs(20).open(file.toFile());
        MutableIntObjectMap<Dbi<DirectBuffer>> dbs = IntObjectMaps.mutable.withInitialCapacity(objectTypes.size());
        objectTypes.each((type) -> {
            dbs.put(type, env.openDbi(Integer.toString(type), MDB_CREATE, MDB_INTEGERKEY));
        });
        this.dbs = dbs;
        this.buff8 = ThreadLocal.withInitial(() -> new UnsafeBuffer(allocateDirect(8)));
    }

    /**
     * Closes the storage.
     */
    @Override
    public void close() {
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

    /**
     * Removes the game object in the database.
     */
    public void removeObject(int type, long id) {
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var key = buff8.get();
            key.putLong(0, id);
            dbs.get(type).delete(txn, key);
            txn.commit();
        }
    }

    private class ObjectsListRecursiveAction<T extends StoredObject> extends AbstractObjectsListRecursiveAction<T> {

        private static final long serialVersionUID = 1L;

        private final int size;

        private final int type;

        protected final BiConsumer<StoredObject, MutableDirectBuffer> writeBuffer;

        public ObjectsListRecursiveAction(int max, int start, int end, List<T> objects, int size, int type,
                BiConsumer<StoredObject, MutableDirectBuffer> writeBuffer) {
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
                    key.putLong(0, o.getId());
                    writeBuffer.accept(o, val);
                    c.put(key, val);
                }
                txn.commit();
            }
        }

        @Override
        protected AbstractObjectsListRecursiveAction<T> create(int max, int start, int end, List<T> objects) {
            return new ObjectsListRecursiveAction<>(max, start, end, objects, size, type, writeBuffer);
        }
    }

    /**
     * Mass storage for game objects.
     */
    public void putObjects(int type, int size, List<? extends StoredObject> objects,
            BiConsumer<StoredObject, MutableDirectBuffer> writeBuffer) {
        int max = 8192;
        if (objects.size() < max) {
            putObjects(type, size, objects, writeBuffer);
        } else {
            var pool = ForkJoinPool.commonPool();
            pool.invoke(new ObjectsListRecursiveAction<>(max, 0, objects.size(), objects, size, type, writeBuffer));
        }
    }

    /**
     * Mass storage for game objects.
     */
    public void putObjects(int type, int size, Iterable<? extends StoredObject> objects,
            BiConsumer<StoredObject, MutableDirectBuffer> writeBuffer) {
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var key = buff8.get();
            final var val = new UnsafeBuffer(allocateDirect(size));
            final var c = dbs.get(type).openCursor(txn);
            for (var o : objects) {
                key.putLong(0, o.getId());
                writeBuffer.accept(o, val);
                c.put(key, val);
            }
            txn.commit();
        }
    }

    /**
     * Retrieves the game object with the specific object ID from the database.
     */
    @SuppressWarnings("unchecked")
    public <T extends StoredObject> T getObject(int type, long id) {
        try (var txn = env.txnRead()) {
            final var key = buff8.get();
            key.putLong(0, id);
            var val = dbs.get(type).get(txn, key);
            return (T) readBuffers.get(type).read(val);
        }
    }

    /**
     * Retrieves all game objects with the specific object type.
     */
    public void getObjects(int type, Consumer<StoredObject> consumer) {
        try (var txn = env.txnRead()) {
            var it = dbs.get(type).iterate(txn);
            it.forEach((k) -> {
                consumer.accept(readBuffers.get(type).read(k.val()));
            });
        }
    }

    /**
     * Retrieves all game objects with the specific object type.
     * <p>
     * The returned {@link DbIterable} must be closed.
     * 
     * <pre>
     * try (var it = storage.getObjects(WorldMap.OBJECT_TYPE)) {
     * }
     * </pre>
     */
    public DbIterable getObjects(int type) {
        var txn = env.txnRead();
        var it = dbs.get(type).iterate(txn);
        return new DbIterable(it, type, txn);
    }

    /**
     * Iterable with {@link AutoCloseable}.
     * 
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public class DbIterable implements Iterable<StoredObject>, Iterator<StoredObject>, AutoCloseable {

        private final Iterator<KeyVal<DirectBuffer>> it;

        private final int type;

        private final Txn<?> txn;

        public DbIterable(CursorIterable<DirectBuffer> it, int type, Txn<?> txn) {
            this.it = it.iterator();
            this.type = type;
            this.txn = txn;
        }

        @Override
        public Iterator<StoredObject> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public StoredObject next() {
            var kv = it.next();
            return readBuffers.get(type).read(kv.val());
        }

        @Override
        public void close() throws Exception {
            txn.reset();
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends GameObject> T get(int type, long key) {
        return (T) getObject(type, key);
    }

    @Override
    public void set(int type, GameObject go) throws ObjectsSetterException {
        var soBuffer = readBuffers.get(type);
        putObject(type, go.id, soBuffer.getSize((StoredObject) go), (b) -> {
            soBuffer.write(b, (StoredObject) go);
        });
    }

    @Override
    public void set(int type, Iterable<GameObject> values) throws ObjectsSetterException {
        var soBuffer = readBuffers.get(type);
        for (var go : values) {
            putObject(type, go.id, soBuffer.getSize((StoredObject) go), (b) -> {
                soBuffer.write(b, (StoredObject) go);
            });
        }
    }

    @Override
    public void remove(int type, GameObject go) throws ObjectsSetterException {
        removeObject(type, go.getId());
    }

}
