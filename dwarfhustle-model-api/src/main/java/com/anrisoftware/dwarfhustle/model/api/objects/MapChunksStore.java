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

import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlocksStore.BLOCK_SIZE_BYTES;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.eclipse.collections.api.factory.primitive.LongObjectMaps;

import com.anrisoftware.dwarfhustle.model.api.objects.MapChunksStoreIndex.Index;

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

    public static final int CHUNK_SIZE_BYTES = MapChunk.SIZE;

    public static int calcIndex(int w, int h, int x, int y, int z) {
        return z * w * h + y * w + x;
    }

    private final MapChunksStoreIndex index;

    private final SeekableByteChannel channel;

    private final ByteBuffer cache;

    private final byte[] buffer;

    public MapChunksStore(Path file, int chunkSize, int chunksCount) throws IOException {
        this.channel = Files.newByteChannel(file, CREATE, READ, WRITE);
        this.buffer = new byte[MapChunk.SIZE + chunkSize * chunkSize * chunkSize * BLOCK_SIZE_BYTES];
        this.cache = ByteBuffer.allocate(MapChunk.SIZE + chunkSize * chunkSize * chunkSize * BLOCK_SIZE_BYTES);
        if (channel.size() > 0) {
            this.index = readIndex(channel);
        } else {
            this.index = new MapChunksStoreIndex(chunksCount);
            writeIndex(channel, index);
        }
    }

    @SneakyThrows
    private void writeIndex(SeekableByteChannel channel, MapChunksStoreIndex index) {
        var stream = Channels.newOutputStream(channel);
        var ostream = new DataOutputStream(stream);
        index.writeStream(ostream);
        ostream.flush();
    }

    @SneakyThrows
    private MapChunksStoreIndex readIndex(SeekableByteChannel channel) {
        var stream = Channels.newInputStream(channel);
        var ostream = new DataInputStream(stream);
        var index = new MapChunksStoreIndex();
        index.readStream(ostream);
        return index;
    }

    public void setChunks(Iterable<MapChunk> chunks) {
        for (var chunk : chunks) {
            setChunk(chunk);
        }
    }

    @SneakyThrows
    public synchronized void setChunk(MapChunk chunk) {
        int i = (int) chunk.getCid();
        var prev = index.map.get(i - 1);
        int pos = prev.pos + prev.size;
        var stream = new ByteArrayOutputStream(buffer.length);
        var dstream = new DataOutputStream(stream);
        chunk.writeStream(dstream);
        dstream.close();
        cache.position(0);
        byte[] bytes = stream.toByteArray();
        cache.limit(cache.capacity());
        cache.put(bytes);
        cache.rewind();
        cache.limit(bytes.length);
        channel.position(pos).write(cache);
        // System.out.println(i + " " + pos + " " + channel.position()); // TODO
        index.map.put(chunk.getCid(), new Index(pos, bytes.length));
        channel.position(0);
        writeIndex(channel, index);
        // System.out.println(channel.position()); // TODO
    }

    @SneakyThrows
    public synchronized MapChunk getChunk(long cid) {
        var idx = index.map.get(cid);
        int skip = idx.pos;
        cache.rewind();
        cache.limit(idx.size);
        channel.position(skip).read(cache);
        cache.flip();
        cache.get(buffer, 0, idx.size);
        var stream = new ByteArrayInputStream(buffer);
        var ostream = new DataInputStream(stream);
        var chunk = new MapChunk();
        chunk.readStream(ostream);
        return chunk;
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
        var map = LongObjectMaps.mutable.withAll(this.index.map);
        map.remove(-1);
        for (var idx : map.values()) {
            cache.rewind();
            cache.limit(idx.size);
            channel.position(idx.pos);
            channel.read(cache);
            cache.flip();
            cache.get(buffer, 0, idx.size);
            var stream = new ByteArrayInputStream(buffer);
            var ostream = new DataInputStream(stream);
            var chunk = new MapChunk();
            chunk.readStream(ostream);
            consumer.accept(chunk);
        }
    }

    public void close() throws IOException {
        System.out.println(channel.size()); // TODO
        channel.close();
    }
}
