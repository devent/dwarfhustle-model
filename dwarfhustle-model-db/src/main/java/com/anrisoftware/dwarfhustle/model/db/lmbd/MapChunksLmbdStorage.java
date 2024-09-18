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
import static org.lmdbjava.PutFlags.MDB_NOOVERWRITE;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Function;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.EnvFlags;
import org.lmdbjava.Txn;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.db.api.MapChunksStorage;
import com.anrisoftware.dwarfhustle.model.db.buffers.MapBlockBuffer;
import com.anrisoftware.dwarfhustle.model.db.buffers.MapChunkBuffer;
import com.google.inject.assistedinject.Assisted;

import jakarta.inject.Inject;

/**
 * Stores the {@link MapChunk}(s) and {@link MapBlock}(s) of the map.
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

    private final ThreadLocal<UnsafeBuffer> buffkey;

    private final ThreadLocal<UnsafeBuffer> buffChunk;

    private final Dbi<DirectBuffer> blocksDb;

    private final ThreadLocal<UnsafeBuffer> buffBlock;

    private final int buffBlockCapacity;

    /**
     * Creates or opens the game map objects storage for the game map.
     */
    @Inject
    protected MapChunksLmbdStorage(@Assisted Path file, @Assisted int csize) {
        this.env = create(PROXY_DB).setMapSize((long) (10 * pow(10, 9))).setMaxDbs(2).open(file.toFile(),
                EnvFlags.MDB_NOLOCK);
        this.chunksDb = env.openDbi("chunks", MDB_CREATE, MDB_INTEGERKEY);
        this.blocksDb = env.openDbi("blocks", MDB_CREATE, MDB_INTEGERKEY);
        this.buffkey = ThreadLocal.withInitial(() -> new UnsafeBuffer(allocateDirect(4)));
        this.buffChunk = ThreadLocal.withInitial(() -> new UnsafeBuffer(allocateDirect(MapChunkBuffer.SIZE)));
        this.buffBlockCapacity = MapBlockBuffer.SIZE * csize * csize * csize;
        this.buffBlock = ThreadLocal.withInitial(() -> {
            return new UnsafeBuffer(allocateDirect(buffBlockCapacity));
        });
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
            final var val = buffChunk.get();
            key.putInt(0, chunk.getCid());
            MapChunkBuffer.write(val, 0, chunk);
            chunksDb.put(txn, key, val);
            if (chunk.isLeaf()) {
                try (final var t = env.txnRead()) {
                    var buff = blocksDb.get(t, key);
                    if (buff == null) {
                        blocksDb.reserve(txn, key, buffBlockCapacity, MDB_NOOVERWRITE);
                    }
                }
            }
            txn.commit();
        }
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
                final var val = buffChunk.get();
                for (int i = start; i < end; i++) {
                    var o = objects.get(i);
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
            final var val = buffChunk.get();
            for (var o : chunks) {
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
     * Stores the {@link MapBlock} in the database.
     */
    @Override
    public void putBlock(MapChunk chunk, MapBlock block) {
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var key = buffkey.get();
            key.putInt(0, chunk.cid);
            var buff = blocksDb.get(txn, key);
            var thatBuff = buffBlock.get();
            thatBuff.putBytes(0, buff, 0, buffBlockCapacity);
            putBlock(key, thatBuff, chunk, block);
            blocksDb.put(txn, key, thatBuff);
            txn.commit();
        }
    }

    /**
     * Stores the {@link MapBlock} in the database.
     */
    @Override
    public void putBlocks(MapChunk chunk, List<MapBlock> blocks) {
        int max = 1024;
        if (blocks.size() < max) {
            putBlocks(chunk, (Iterable<MapBlock>) blocks);
        } else {
            var pool = ForkJoinPool.commonPool();
            pool.invoke(new BlocksListRecursiveAction(chunk, max, 0, blocks.size(), blocks));
        }
    }

    private void putBlocks(MapChunk chunk, Iterable<MapBlock> blocks) {
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var key = buffkey.get();
            key.putInt(0, chunk.cid);
            var buff = blocksDb.get(txn, key);
            var thatBuff = buffBlock.get();
            thatBuff.putBytes(0, buff, 0, buffBlockCapacity);
            for (MapBlock block : blocks) {
                putBlock(key, thatBuff, chunk, block);
            }
            blocksDb.put(txn, key, thatBuff);
            txn.commit();
        }
    }

    private void putBlock(DirectBuffer key, MutableDirectBuffer buff, MapChunk chunk, MapBlock block) {
        int i = GameBlockPos.calcIndex(chunk.pos.getSizeX(), chunk.pos.getSizeY(), chunk.pos.getSizeZ(), chunk.pos.x,
                chunk.pos.y, chunk.pos.z, block.pos.x, block.pos.y, block.pos.z);
        MapBlockBuffer.write(buff, i * MapBlockBuffer.SIZE, block);
    }

    private class BlocksListRecursiveAction extends AbstractObjectsListRecursiveAction<MapBlock> {

        private static final long serialVersionUID = 1L;

        private final MapChunk chunk;

        public BlocksListRecursiveAction(MapChunk chunk, int max, int start, int end, List<MapBlock> objects) {
            super(max, start, end, objects);
            this.chunk = chunk;
        }

        @Override
        protected void processing() {
            try (Txn<DirectBuffer> txn = env.txnWrite()) {
                final var key = buffkey.get();
                key.putInt(0, chunk.cid);
                var buff = blocksDb.get(txn, key);
                var thatBuff = buffBlock.get();
                thatBuff.putBytes(0, buff, 0, buffBlockCapacity);
                for (int i = start; i < end; i++) {
                    var o = objects.get(i);
                    putBlock(key, thatBuff, chunk, o);
                }
                blocksDb.put(txn, key, thatBuff);
                txn.commit();
            }
        }

        @Override
        protected AbstractObjectsListRecursiveAction<MapBlock> create(int max, int start, int end,
                List<MapBlock> objects) {
            return new BlocksListRecursiveAction(chunk, max, start, end, objects);
        }
    }

    /**
     * Returns the {@link MapBlock} from the database.
     */
    @Override
    public MapBlock getBlock(MapChunk chunk, GameBlockPos pos) {
        try (final var t = env.txnRead()) {
            final var key = buffkey.get();
            key.putInt(0, chunk.cid);
            var buff = blocksDb.get(t, key);
            int i = GameBlockPos.calcIndex(chunk.pos.getSizeX(), chunk.pos.getSizeY(), chunk.pos.getSizeZ(),
                    chunk.pos.x, chunk.pos.y, chunk.pos.z, pos.x, pos.y, pos.z);
            return MapBlockBuffer.read(buff, i * MapBlockBuffer.SIZE, pos);
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

    /**
     * Retrieves a {@link MapBlock}'s buffer for the {@link MapChunk}.
     */
    @Override
    public void withBlockBuffer(MapChunk chunk, Consumer<MutableDirectBuffer> consumer) {
        try (Txn<DirectBuffer> txn = env.txnWrite()) {
            final var key = buffkey.get();
            key.putInt(0, chunk.cid);
            var buff = blocksDb.get(txn, key);
            var thatBuff = buffBlock.get();
            thatBuff.putBytes(0, buff, 0, buffBlockCapacity);
            consumer.accept(thatBuff);
            blocksDb.put(txn, key, thatBuff);
            txn.commit();
        }
    }

    /**
     * Retrieves a {@link MapBlock}'s buffer for the {@link MapChunk}.
     */
    @Override
    public <T> T withBlockReadBuffer(MapChunk chunk, Function<DirectBuffer, T> consumer) {
        try (final var t = env.txnRead()) {
            final var key = buffkey.get();
            key.putInt(0, chunk.cid);
            var buff = blocksDb.get(t, key);
            return consumer.apply(buff);
        }
    }

}
