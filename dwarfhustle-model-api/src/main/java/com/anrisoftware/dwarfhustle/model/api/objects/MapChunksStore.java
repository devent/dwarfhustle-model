/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.api.objects;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

import lombok.SneakyThrows;

/**
 * Stores {@link MapChunk}s from an indexed file.
 *
 * <ul>
 * <li>4x4x4 2 9 64 = 41697 bytes = 42 KB 41697
 * <li>8x8x8 2 73 512 = 338209 bytes = 338 KB
 * <li>32x32x32 4 585 32768 = 19483425 bytes = 20 MB
 * <li>128x128x128 16 585 2097152 = 1227148065 bytes = 1,227 GB
 * <li>256x256x128 16 585 8388608 = 1227148065 bytes = 1,227 GB
 * </ul>
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class MapChunksStore {

    public static int calcIndex(int w, int h, int x, int y, int z) {
        return z * w * h + y * w + x;
    }

    private final FileChannel channel;

    private final long indexSize;

    private final MappedByteBuffer indexBuffer;

    private final MutableIntObjectMap<MapChunk> chunksCache;

    private final int width;

    private final int height;

    public MapChunksStore(Path file, int width, int height, int chunkSize, int chunksCount) throws IOException {
        this.channel = FileChannel.open(file, CREATE, READ, WRITE);
        boolean newFile = channel.size() == 0;
        this.indexSize = MapChunksIndexBuffer.SIZE_MIN + (chunksCount + 1) * MapChunksIndexBuffer.SIZE_ENTRY;
        this.indexBuffer = channel.map(MapMode.READ_WRITE, 0, indexSize);
        this.chunksCache = IntObjectMaps.mutable.ofInitialCapacity(chunksCount);
        this.width = width;
        this.height = height;
        if (newFile) {
            writeInitialIndex(chunksCount);
        } else {
            readChunks(chunksCount, width, height);
        }
    }

    @SneakyThrows
    private void writeInitialIndex(int chunksCount) {
        chunksCount++;
        int[] entries = new int[chunksCount * 2];
        entries[0] = 0;
        entries[1] = MapChunksIndexBuffer.SIZE_MIN + chunksCount * MapChunksIndexBuffer.SIZE_ENTRY;
        MapChunksIndexBuffer.setEntries(indexBuffer, 0, chunksCount, entries);
    }

    private void readChunks(int chunksCount, int width, int height) throws IOException {
        var entries = MapChunksIndexBuffer.getEntries(indexBuffer, 0, null);
        for (int i = 1; i < entries.length / 2; i++) {
            int pos = entries[i * 2 + 0];
            int size = entries[i * 2 + 1];
            var buffer = channel.map(MapMode.READ_WRITE, pos, size);
            var chunk = MapChunkBuffer.readMapChunk(buffer, 0);
            chunk.updateCenterExtent(width, height);
            chunksCache.put(chunk.cid, chunk);
        }
    }

    public void setChunks(Iterable<MapChunk> chunks) {
        for (var chunk : chunks) {
            setChunk(chunk);
        }
    }

    @SneakyThrows
    public synchronized void setChunk(MapChunk chunk) {
        int i = chunk.cid;
        int ppos = MapChunksIndexBuffer.getPos(indexBuffer, 0, i);
        int psize = MapChunksIndexBuffer.getSize(indexBuffer, 0, i);
        int pos = ppos + psize;
        int size = getChunkSize(chunk);
        var buffer = channel.map(MapMode.READ_WRITE, pos, size);
        MapChunkBuffer.writeMapChunk(buffer, 0, chunk);
        MapChunksIndexBuffer.setEntry(indexBuffer, 0, chunk.cid + 1, pos, size);
        chunk.updateCenterExtent(width, height);
        chunksCache.put(chunk.cid, chunk);
    }

    private synchronized int getChunkSize(MapChunk chunk) {
        if (chunk.isLeaf()) {
            int size = MapChunkBuffer.SIZE_LEAF_MIN;
            size += chunk.getBlocksBuffer().capacity();
            return size;
        } else {
            return MapChunkBuffer.SIZE_MIN;
        }
    }

    public synchronized MapChunk getChunk(int cid) {
        return chunksCache.get(cid);
    }

    /**
     * Finds the {@link MapChunk} with the {@link GameChunkPos}.
     */
    public synchronized Optional<MapChunk> findChunk(GameChunkPos pos) {
        for (var chunk : getChunks()) {
            if (chunk.pos.equals(pos)) {
                return Optional.of(chunk);
            }
        }
        return Optional.empty();
    }

    /**
     * Finds the {@link MapChunk} and {@link MapBlock} with the
     * {@link GameBlockPos}.
     */
    public synchronized Optional<Pair<MapChunk, MapBlock>> findBlock(GameBlockPos pos) {
        if (pos.isNegative()) {
            return Optional.empty();
        }
        for (var chunk : getChunks()) {
            if (chunk.isInside(pos)) {
                return findBlock(chunk, pos);
            }
        }
        return Optional.empty();
    }

    /**
     * Finds the {@link MapChunk} and {@link MapBlock} with the
     * {@link GameBlockPos}.
     */
    public synchronized Optional<Pair<MapChunk, MapBlock>> findBlock(MapChunk chunk, GameBlockPos pos) {
        for (var chunks : chunk.getChunks().keyValuesView()) {
            if (chunks.getTwo().contains(pos)) {
                return findBlock(getChunk(chunks.getOne()), pos);
            }
        }
        return Optional.of(Tuples.pair(chunk, chunk.getBlock(pos)));
    }

    /**
     * Iterates over all {@link MapChunk}s in the storage. The order is not
     * necessarily the same as the order the chunks were set.
     */
    public void forEachValue(Consumer<MapChunk> consumer) {
        chunksCache.asSynchronized().forEach(consumer);
    }

    /**
     * Returns an {@link Iterable} over all {@link MapChunk}.
     */
    public Iterable<MapChunk> getChunks() {
        return chunksCache.asSynchronized().asLazy();
    }

    /**
     * Returns the file mapped buffer for the blocks for the {@link MapChunk}. The
     * method is used to create map chunks. A buffer is already mapped for already
     * loaded map chunks.
     */
    @SneakyThrows
    public synchronized ByteBuffer getBlocksBuffer(MapChunk chunk) {
        int ppos = MapChunksIndexBuffer.getPos(indexBuffer, 0, chunk.cid);
        int psize = MapChunksIndexBuffer.getSize(indexBuffer, 0, chunk.cid);
        int pos = ppos + psize + MapChunkBuffer.SIZE_LEAF_MIN;
        int size = MapBlockBuffer.calcMapBufferSize(chunk.pos.getSizeX(), chunk.pos.getSizeY(), chunk.pos.getSizeZ());
        return channel.map(MapMode.READ_WRITE, pos, size);
    }

    public void close() throws IOException {
        channel.close();
    }

}
