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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;

public class LmbdStorage {

    private final Env<DirectBuffer> env;

    private final Dbi<DirectBuffer> dbPosIds;

    private final int w;

    private final int h;

    private final int d;

    private final Txn<DirectBuffer> readTxn;

    private final Dbi<DirectBuffer> dbIdObject;

    public LmbdStorage(Path file, GameMap gm) {
        this.w = gm.width;
        this.h = gm.height;
        this.d = gm.depth;
        this.env = create(PROXY_DB).setMapSize((long) (10 * pow(10, 9))).setMaxDbs(2).open(file.toFile());
        this.dbPosIds = env.openDbi("pos-ids", MDB_CREATE, MDB_DUPSORT, MDB_DUPFIXED, MDB_INTEGERDUP);
        this.dbIdObject = env.openDbi("id-object", MDB_CREATE, MDB_INTEGERKEY);
        this.readTxn = env.txnRead();
        readTxn.reset();
    }

    public void close() {
        readTxn.close();
        env.close();
    }

    public void putObject(int x, int y, int z, int size, long id, Consumer<MutableDirectBuffer> writeBuffer) {
        int index = GameBlockPos.calcIndex(w, h, d, 0, 0, 0, x, y, z);
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var key = new UnsafeBuffer(allocateDirect(4));
            key.putInt(0, index);
            final var val = new UnsafeBuffer(allocateDirect(8));
            val.putLong(0, id);
            dbPosIds.put(txn, key, val);
            txn.commit();
        }
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var key = new UnsafeBuffer(allocateDirect(8));
            key.putLong(0, id);
            final var val = new UnsafeBuffer(allocateDirect(size));
            writeBuffer.accept(val);
            dbIdObject.put(txn, key, val);
            txn.commit();
        }
    }

    public void putObjects(int size, Iterable<GameMapObject> objects,
            BiConsumer<GameMapObject, MutableDirectBuffer> writeBuffer) {
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var key = new UnsafeBuffer(allocateDirect(4));
            final var val = new UnsafeBuffer(allocateDirect(8));
            final var c = dbPosIds.openCursor(txn);
            for (var o : objects) {
                int index = GameBlockPos.calcIndex(w, h, d, 0, 0, 0, o.pos.x, o.pos.y, o.pos.z);
                key.putInt(0, index);
                val.putLong(0, o.id);
                c.put(key, val);
            }
            txn.commit();
        }
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var key = new UnsafeBuffer(allocateDirect(8));
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

    public <T extends GameObject> T getObject(long id, Function<DirectBuffer, T> readBuffer) {
        try {
            final var key = new UnsafeBuffer(allocateDirect(8));
            readTxn.renew();
            key.putLong(0, id);
            var val = dbIdObject.get(readTxn, key);
            return readBuffer.apply(val);
        } finally {
            readTxn.reset();
        }
    }

    public <T extends GameObject> void getObjects(int x, int y, int z, Function<DirectBuffer, T> readBuffer,
            Consumer<T> consumer) {
        try {
            readTxn.renew();
            final var bkey = new UnsafeBuffer(allocateDirect(4));
            final int index = GameBlockPos.calcIndex(w, h, d, 0, 0, 0, x, y, z);
            bkey.putInt(0, index);
            final var c = dbPosIds.openCursor(readTxn);
            if (!c.get(bkey, MDB_SET)) {
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

    public <T extends GameObject> void getObjectsRange(int sx, int sy, int sz, int ex, int ey, int ez,
            Function<DirectBuffer, T> readBuffer, Consumer<T> consumer) {
        final var bkey = new UnsafeBuffer(allocateDirect(4));
        final int xx = ex - sx;
        final int yy = ey - sy;
        final int zz = ez - sz;
        try {
            readTxn.renew();
            final var c = dbPosIds.openCursor(readTxn);
            for (int x = sx; x < xx; x++) {
                for (int y = sy; y < yy; y++) {
                    for (int z = sz; z < zz; z++) {
                        final int index = GameBlockPos.calcIndex(w, h, d, 0, 0, 0, x, y, z);
                        bkey.putInt(0, index);
                        if (!c.get(bkey, MDB_SET)) {
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
}
