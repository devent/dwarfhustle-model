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
import java.util.function.Consumer;

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

    private long indexSize;

    private MappedByteBuffer indexBuffer;

    public MapChunksStore(Path file, int chunkSize, int chunksCount) throws IOException {
        this.channel = FileChannel.open(file, CREATE, READ, WRITE);
        this.indexSize = MapChunksIndexBuffer.SIZE_MIN + chunksCount * MapChunksIndexBuffer.SIZE_ENTRY;
        this.indexBuffer = channel.map(MapMode.READ_WRITE, 0, indexSize);
        if (channel.size() == 0) {
            writeIndex(chunksCount);
        }
    }

    @SneakyThrows
    private void writeIndex(int chunksCount) {
        int[] entries = new int[chunksCount * 3];
        MapChunksIndexBuffer.setEntries(indexBuffer, 0, chunksCount, entries);
    }

    public void setChunks(Iterable<MapChunk> chunks) {
        for (var chunk : chunks) {
            setChunk(chunk);
        }
    }

    @SneakyThrows
    public synchronized void setChunk(MapChunk chunk) {
        int i = chunk.cid;
        int pos = MapChunksIndexBuffer.getPos(indexBuffer, 0, i);
        int size = MapChunksIndexBuffer.getSize(indexBuffer, 0, i);
        var buffer = channel.map(MapMode.READ_WRITE, pos, size);
        MapChunkBuffer.writeMapChunk(buffer, 0, chunk);
    }

    @SneakyThrows
    public synchronized MapChunk getChunk(int cid) {
        int i = cid;
        int pos = MapChunksIndexBuffer.getPos(indexBuffer, 0, i);
        int size = MapChunksIndexBuffer.getSize(indexBuffer, 0, i);
        var buffer = channel.map(MapMode.READ_WRITE, pos, size);
        return MapChunkBuffer.readMapChunk(buffer, 0);
    }

    @SneakyThrows
    public synchronized ByteBuffer getBlocksBuffer(int cid) {
        int pos = MapChunksIndexBuffer.getPos(indexBuffer, 0, cid);
        int size = MapChunksIndexBuffer.getSize(indexBuffer, 0, cid);
        return channel.map(MapMode.READ_WRITE, pos, size);
    }

    /**
     * Iterates over all {@link MapChunk}s in the storage. The order is not
     * necessarily the same as the order the chunks were set.
     */
    @SneakyThrows
    public synchronized void forEachValue(Consumer<MapChunk> consumer) {
        if (channel.size() == 0) {
            return;
        }
        var entries = MapChunksIndexBuffer.getEntries(indexBuffer, 0, null);
        for (int i = 0; i < entries.length / 3; i++) {
            int pos = entries[i * 3 + 1];
            int size = entries[i * 3 + 2];
            var buffer = channel.map(MapMode.READ_WRITE, pos, size);
            var chunk = MapChunkBuffer.readMapChunk(buffer, 0);
            consumer.accept(chunk);
        }
    }

    public void close() throws IOException {
        System.out.println(channel.size()); // TODO
        channel.close();
    }

}
