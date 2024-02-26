package com.anrisoftware.dwarfhustle.model.api.objects;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import lombok.Data;
import lombok.SneakyThrows;

/**
 * Stores {@link MapBlock} as an indexed byte array.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class MapBlocksStore implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int BLOCK_SIZE_BYTES = 2048;

    @Data
    private static class MapBlockItem implements Serializable {

        private static final long serialVersionUID = 1L;

        final int index;

        final int size;

        final MapBlock block;
    }

    public static int calcIndex(int w, int h, int x, int y, int z) {
        return z * w * h + y * w + x;
    }

    private byte[] buffer;

    private int chunkSize;

    private int size;

    public MapBlocksStore(int chunkSize) {
        this.chunkSize = chunkSize;
        this.size = chunkSize * chunkSize * chunkSize;
        this.buffer = new byte[BLOCK_SIZE_BYTES * size];
    }

    @SneakyThrows
    public synchronized void setBlock(MapBlock block) {
        int index = calcIndex(chunkSize, chunkSize, block.pos.x, block.pos.y, block.pos.z) % size;
        int pos = index * BLOCK_SIZE_BYTES;
        var stream = new ByteArrayOutputStream(BLOCK_SIZE_BYTES);
        new ObjectOutputStream(stream).writeObject(block);
        int size = stream.size();
        stream.flush();
        stream.close();
        System.arraycopy(stream.toByteArray(), 0, buffer, pos, size);
    }

    @SneakyThrows
    public synchronized MapBlock getBlock(GameBlockPos pos) {
        int index = calcIndex(chunkSize, chunkSize, pos.x, pos.y, pos.z) % size;
        int skip = index * BLOCK_SIZE_BYTES;
        var stream = new ByteArrayInputStream(buffer);
        stream.skip(skip);
        var ostream = new ObjectInputStream(stream);
        var block = (MapBlock) ostream.readObject();
        return block;
    }

}
