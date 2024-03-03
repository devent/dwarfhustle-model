package com.anrisoftware.dwarfhustle.model.api.objects;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.function.Consumer;

import lombok.Data;
import lombok.SneakyThrows;

/**
 * Stores {@link MapBlock} as an indexed byte array.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class MapBlocksStore implements Serializable, Externalizable {

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

    private boolean empty = true;

    public MapBlocksStore(int chunkSize) {
        this.chunkSize = chunkSize;
        this.size = chunkSize * chunkSize * chunkSize;
        this.buffer = new byte[BLOCK_SIZE_BYTES * size];
    }

    public void setBlocks(Iterable<MapBlock> blocks) {
        for (var block : blocks) {
            setBlock(block);
        }
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
        this.empty = false;
    }

    @SneakyThrows
    public synchronized MapBlock getBlock(GameBlockPos pos) {
        if (empty) {
            return null;
        }
        int index = calcIndex(chunkSize, chunkSize, pos.x, pos.y, pos.z) % size;
        int skip = index * BLOCK_SIZE_BYTES;
        var stream = new ByteArrayInputStream(buffer);
        stream.skip(skip);
        var ostream = new ObjectInputStream(stream);
        var block = (MapBlock) ostream.readObject();
        return block;
    }

    /**
     * Iterates over all {@link MapBlock}s in the storage. The order is not
     * necessarily the same as the order the blocks were set.
     */
    @SneakyThrows
    public synchronized void forEachValue(Consumer<MapBlock> consumer) {
        if (empty) {
            return;
        }
        for (int i = 0; i < size; i++) {
            var stream = new ByteArrayInputStream(buffer);
            stream.skip(i * BLOCK_SIZE_BYTES);
            var ostream = new ObjectInputStream(stream);
            consumer.accept((MapBlock) ostream.readObject());
        }
    }

    public synchronized boolean isEmpty() {
        return empty;
    }

    public synchronized void setData(byte[] data) {
        this.buffer = data;
    }

    public synchronized byte[] getData() {
        return buffer;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeBoolean(empty);
        if (!empty) {
            out.write(buffer);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.empty = in.readBoolean();
        if (!empty) {
            in.readFully(buffer);
        }
    }

}
