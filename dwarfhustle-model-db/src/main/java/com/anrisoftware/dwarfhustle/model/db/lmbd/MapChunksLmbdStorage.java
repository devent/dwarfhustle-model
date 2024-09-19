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
import java.util.function.Consumer;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.EnvFlags;
import org.lmdbjava.Txn;

import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.db.api.MapChunksStorage;
import com.anrisoftware.dwarfhustle.model.db.buffers.MapChunkBuffer;
import com.google.inject.assistedinject.Assisted;

import jakarta.inject.Inject;

/**
 * Stores the {@link MapChunk}(s) of the map.
 */
public class MapChunksLmbdStorage implements MapChunksStorage {

    /**
     * Factory to create the {@link MapChunksLmbdStorage}.
     * 
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface MapChunksLmbdStorageFactory {
        MapChunksLmbdStorage create(Path file, int chuckSize);
    }

    private final Env<DirectBuffer> env;

    private final Dbi<DirectBuffer> chunksDb;

    private final ThreadLocal<MutableDirectBuffer> buffkey;

    private final ThreadLocal<MutableDirectBuffer> buffChunk;

    /**
     * Creates or opens the game map objects storage for the game map.
     */
    @Inject
    protected MapChunksLmbdStorage(@Assisted Path file, @Assisted int csize) {
        this.env = create(PROXY_DB).setMapSize((long) (10 * pow(10, 9))).setMaxDbs(2).open(file.toFile(),
                EnvFlags.MDB_NOLOCK);
        this.chunksDb = env.openDbi("chunks", MDB_CREATE, MDB_INTEGERKEY);
        this.buffkey = ThreadLocal.withInitial(() -> new UnsafeBuffer(allocateDirect(4)));
        this.buffChunk = ThreadLocal.withInitial(() -> MapChunkBuffer.createBlocks(MapChunkBuffer.SIZE_MIN));
    }

    /**
     * Closes the storage.
     */
    @Override
    public void close() {
        env.close();
    }

    /**
     * Stores the {@link MapChunk} in the database.
     */
    @Override
    public void putChunk(MapChunk chunk) {
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var key = buffkey.get();
            final var val = createBuffBlocks(chunk);
            key.putInt(0, chunk.getCid());
            MapChunkBuffer.write(val, 0, chunk);
            chunksDb.put(txn, key, val);
            txn.commit();
        }
    }

    private MutableDirectBuffer createBuffBlocks(MapChunk chunk) {
        final MutableDirectBuffer val;
        if (chunk.isLeaf()) {
            val = createBuffBlocksLeaf(chunk);
        } else {
            val = buffChunk.get();
        }
        return val;
    }

    private MutableDirectBuffer createBuffBlocksLeaf(MapChunk chunk) {
        return new UnsafeBuffer(allocateDirect(MapChunkBuffer.SIZE_MIN
                + MapChunkBuffer.getBlocksSize(chunk.pos.getSizeX(), chunk.pos.getSizeY(), chunk.pos.getSizeZ())));
    }

    private class ChunksListRecursiveAction extends AbstractObjectsListRecursiveAction<MapChunk> {

        private static final long serialVersionUID = 1L;

        public ChunksListRecursiveAction(int max, int start, int end, List<MapChunk> objects) {
            super(max, start, end, objects);
        }

        @Override
        protected void processing() {
            try (Txn<DirectBuffer> txn = env.txnWrite()) {
                final var c = chunksDb.openCursor(txn);
                final var key = buffkey.get();
                for (int i = start; i < end; i++) {
                    final var o = objects.get(i);
                    final var val = createBuffBlocks(o);
                    key.putInt(0, o.cid);
                    MapChunkBuffer.write(val, 0, o);
                    c.put(key, val);
                }
                txn.commit();
            }
        }

        @Override
        protected AbstractObjectsListRecursiveAction<MapChunk> create(int max, int start, int end,
                List<MapChunk> objects) {
            return new ChunksListRecursiveAction(max, start, end, objects);
        }
    }

    /**
     * Mass storage for {@link MapChunk}(s).
     */
    @Override
    public void putChunks(List<MapChunk> chunks) {
        int max = 8192;
        if (chunks.size() < max) {
            putChunks((Iterable<MapChunk>) chunks);
        } else {
            var pool = ForkJoinPool.commonPool();
            pool.invoke(new ChunksListRecursiveAction(max, 0, chunks.size(), chunks));
        }
    }

    /**
     * Mass storage for {@link MapChunk}(s).
     */
    @Override
    public void putChunks(Iterable<MapChunk> chunks) {
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var c = chunksDb.openCursor(txn);
            final var key = buffkey.get();
            for (final var o : chunks) {
                final var val = createBuffBlocks(o);
                key.putInt(0, o.cid);
                MapChunkBuffer.write(val, 0, o);
                c.put(key, val);
            }
            txn.commit();
        }
    }

    /**
     * Returns the {@link MapChunk} with the chunk ID.
     */
    @Override
    public MapChunk getChunk(int cid) {
        try (final var t = env.txnRead()) {
            final var key = buffkey.get();
            key.putInt(0, cid);
            var val = chunksDb.get(t, key);
            return MapChunkBuffer.read(val, 0);
        }
    }

    /**
     * Retrieves all {@link MapChunk} chunks.
     */
    @Override
    public void forEachValue(Consumer<MapChunk> consumer) {
        try (final var t = env.txnRead()) {
            var it = chunksDb.iterate(t);
            it.forEach((k) -> {
                consumer.accept(MapChunkBuffer.read(k.val(), 0));
            });
        }
    }

}
