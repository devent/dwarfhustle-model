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
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
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

    public static final int CHUNK_SIZE_BYTES = MapChunk.SIZE;

    public static int calcIndex(int w, int h, int x, int y, int z) {
        return z * w * h + y * w + x;
    }

    private final SeekableByteChannel channel;

    private final ByteBuffer cache;

    private final byte[] buffer;

    public MapChunksStore(Path file, int chunkSize) throws IOException {
        this.channel = Files.newByteChannel(file, CREATE, READ, WRITE);
        this.buffer = new byte[MapChunk.SIZE + chunkSize * chunkSize * chunkSize * BLOCK_SIZE_BYTES];
        this.cache = ByteBuffer.allocate(MapChunk.SIZE + chunkSize * chunkSize * chunkSize * BLOCK_SIZE_BYTES);
    }

    public void setChunks(Iterable<MapChunk> chunks) {
        for (var chunk : chunks) {
            setChunk(chunk);
        }
    }

    @SneakyThrows
    public synchronized void setChunk(MapChunk chunk) {
        int index = (int) chunk.getCid();
        int pos = index * buffer.length;
        var stream = new ByteArrayOutputStream(buffer.length);
        var dstream = new DataOutputStream(stream);
        chunk.writeStream(dstream);
        dstream.close();
        cache.position(0);
        cache.put(stream.toByteArray());
        cache.rewind();
        channel.position(pos).write(cache);
    }

    @SneakyThrows
    public synchronized MapChunk getChunk(long cid) {
        int index = (int) cid;
        int skip = index * CHUNK_SIZE_BYTES;
        channel.position(skip).read(cache);
        cache.get(buffer);
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
        channel.position(0);
        while (channel.read(cache) > 0) {
            cache.put(buffer);
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
