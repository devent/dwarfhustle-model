package com.anrisoftware.dwarfhustle.model.db.lmbd;

import static java.lang.Math.pow;
import static java.nio.ByteBuffer.allocateDirect;
import static org.lmdbjava.DbiFlags.MDB_CREATE;
import static org.lmdbjava.DirectBufferProxy.PROXY_DB;
import static org.lmdbjava.Env.create;

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

    private final Dbi<DirectBuffer> db;

    private final int w;

    private final int h;

    private final int d;

    private final Txn<DirectBuffer> readTxn;

    public LmbdStorage(Path file, GameMap gm) {
        this.w = gm.width;
        this.h = gm.height;
        this.d = gm.depth;
        this.env = create(PROXY_DB).setMapSize((long) (10 * pow(10, 9))).setMaxDbs(1).open(file.toFile());
        this.db = env.openDbi(Long.toString(gm.id), MDB_CREATE);
        this.readTxn = env.txnRead();
        readTxn.reset();
    }

    public void close() {
        readTxn.close();
        env.close();
    }

    public void putObject(int x, int y, int z, int size, Consumer<MutableDirectBuffer> writeBuffer) {
        int index = GameBlockPos.calcIndex(w, h, d, 0, 0, 0, x, y, z);
        final MutableDirectBuffer key = new UnsafeBuffer(allocateDirect(4));
        final MutableDirectBuffer val = new UnsafeBuffer(allocateDirect(size));
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            writeBuffer.accept(val);
            key.putInt(0, index);
            db.put(txn, key, val);
            txn.commit();
        }
    }

    public void putObjects(int size, Iterable<GameMapObject> objects,
            BiConsumer<GameMapObject, MutableDirectBuffer> writeBuffer) {
        final MutableDirectBuffer key = new UnsafeBuffer(allocateDirect(4));
        final MutableDirectBuffer val = new UnsafeBuffer(allocateDirect(size));
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var c = db.openCursor(txn);
            for (var o : objects) {
                int index = GameBlockPos.calcIndex(w, h, d, 0, 0, 0, o.pos.x, o.pos.y, o.pos.z);
                key.putInt(0, index);
                writeBuffer.accept(o, val);
                c.put(key, val);
            }
            txn.commit();
        }
    }

    public <T extends GameObject> T getObject(int x, int y, int z, Function<DirectBuffer, T> readBuffer) {
        int index = GameBlockPos.calcIndex(w, h, d, 0, 0, 0, x, y, z);
        final MutableDirectBuffer key = new UnsafeBuffer(allocateDirect(4));
        try {
            readTxn.renew();
            key.putInt(0, index);
            var val = db.get(readTxn, key);
            return readBuffer.apply(val);
        } finally {
            readTxn.reset();
        }
    }

    public <T extends GameObject> void getObjectsRange(int sx, int sy, int sz, int ex, int ey, int ez,
            Function<DirectBuffer, T> readBuffer, Consumer<T> consumer) {
        final MutableDirectBuffer key = new UnsafeBuffer(allocateDirect(4));
        final int xx = ex - sx;
        final int yy = ey - sy;
        final int zz = ez - sz;
        try {
            readTxn.renew();
            for (int x = sx; x < xx; x++) {
                for (int y = sy; y < yy; y++) {
                    for (int z = sz; z < zz; z++) {
                        final int index = GameBlockPos.calcIndex(w, h, d, 0, 0, 0, x, y, z);
                        key.putInt(0, index);
                        var val = db.get(readTxn, key);
                        var o = readBuffer.apply(val);
                        consumer.accept(o);
                    }
                }
            }
        } finally {
            readTxn.reset();
        }
    }
}
