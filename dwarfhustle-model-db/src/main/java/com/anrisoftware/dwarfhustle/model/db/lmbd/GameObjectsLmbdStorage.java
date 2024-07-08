package com.anrisoftware.dwarfhustle.model.db.lmbd;

import static java.lang.Math.pow;
import static java.nio.ByteBuffer.allocateDirect;
import static org.lmdbjava.DbiFlags.MDB_CREATE;
import static org.lmdbjava.DbiFlags.MDB_INTEGERKEY;
import static org.lmdbjava.DirectBufferProxy.PROXY_DB;
import static org.lmdbjava.Env.create;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.eclipse.collections.api.factory.Maps;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;

/**
 * LMBD storage for game objects that are not on the game map.
 */
public class GameObjectsLmbdStorage {

    private final Env<DirectBuffer> env;

    private final Map<String, Dbi<DirectBuffer>> dbs;

    private final Txn<DirectBuffer> readTxn;

    public GameObjectsLmbdStorage(Path file, int dbcount, Function<Integer, String> dbsNames) {
        this.env = create(PROXY_DB).setMapSize((long) (10 * pow(10, 9))).setMaxDbs(20).open(file.toFile());
        this.dbs = Maps.mutable.withInitialCapacity(dbcount);
        for (int i = 0; i < dbcount; i++) {
            String name = dbsNames.apply(i);
            dbs.put(name, env.openDbi(name, MDB_CREATE, MDB_INTEGERKEY));
        }
        this.readTxn = env.txnRead();
        readTxn.reset();
    }

    public void close() {
        readTxn.close();
        env.close();
    }

    public void putObject(String type, long id, int size, Consumer<MutableDirectBuffer> writeBuffer) {
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var key = new UnsafeBuffer(allocateDirect(8));
            key.putLong(0, id);
            final var val = new UnsafeBuffer(allocateDirect(size));
            writeBuffer.accept(val);
            dbs.get(type).put(txn, key, val);
            txn.commit();
        }
    }

    public void putObjects(String type, int size, Iterable<GameMapObject> objects,
            BiConsumer<GameMapObject, MutableDirectBuffer> writeBuffer) {
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var key = new UnsafeBuffer(allocateDirect(8));
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

    public <T extends GameObject> T getObject(String type, long id, Function<DirectBuffer, T> readBuffer) {
        try {
            final var key = new UnsafeBuffer(allocateDirect(8));
            readTxn.renew();
            key.putLong(0, id);
            var val = dbs.get(type).get(readTxn, key);
            return readBuffer.apply(val);
        } finally {
            readTxn.reset();
        }
    }

}
